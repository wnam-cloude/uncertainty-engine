#!/usr/bin/env python3
"""Create proper Android binary XML manifest"""
import struct
import zipfile
import os
import subprocess

SDK = "/usr/lib/android-sdk"
BUILD_TOOLS_29 = f"{SDK}/build-tools/29.0.3"
BUILD_TOOLS_34 = f"{SDK}/build-tools/34.0.0"
PLATFORM = f"{SDK}/platforms/android-36"
ANDROID_JAR = f"{PLATFORM}/android.jar"
APP_DIR = "/root/uncertainty-engine/android/app"
OUT = "/root/uncertainty-engine/build-manual/out"

def encode_string_pool(strings):
    """Encode strings as Android string pool."""
    unique = []
    for s in strings:
        if s not in unique:
            unique.append(s)

    # UTF-16LE encode with 4-byte alignment
    encoded = []
    for s in unique:
        data = s.encode('utf-16-le')
        while len(data) % 4:
            data += b'\x00'
        encoded.append(data)

    # Calculate total size
    offset = 0
    offsets = []
    for e in encoded:
        offsets.append(offset)
        offset += 4 + len(e)

    # Chunk header
    header = struct.pack('<HHII',
        0x001C,  # type
        len(unique),  # string count
        0,         # style count
        8 + 4 * len(unique) + sum(len(e) for e in encoded)  # chunk size
    )

    data = header + b''.join(struct.pack('<I', o) for o in offsets) + b''.join(encoded)
    return data, {s: i for i, s in enumerate(unique)}

def encode_start_tag(name_idx, attrs=None):
    """Encode a start tag."""
    # TAG_START_TAG = 0x00100112
    attr_count = len(attrs) if attrs else 0
    attr_offset = 8 + attr_count * 28 if attr_count > 0 else 0

    header = struct.pack('<HBBBBHH',
        0x0010,       # type
        0,            # info (line number)
        0xFF,         # NS index (-1 = none)
        name_idx,     # name index
        attr_count,   # attribute count
        attr_offset,  # attribute start offset
        0             # comment index
    )

    attrs_data = b''
    if attrs:
        for ns_idx, name_idx, value_idx, res_type, res_data in attrs:
            # Build the 4-byte typed value (2 bytes type + 2 bytes data)
            typed_value = (res_type << 16) | (res_data & 0xFFFF)
            attrs_data += struct.pack('<HHHI',
                ns_idx,         # namespace index (2 bytes)
                name_idx,       # name index (2 bytes)
                value_idx,      # string value index (2 bytes)
                typed_value     # typed value (4 bytes)
            )

    return header + attrs_data

def encode_end_tag(name_idx):
    """Encode an end tag."""
    # TAG_END_TAG = 0x00100113
    return struct.pack('<HBBH',
        0x0010,       # type
        0,            # info
        0xFF,         # NS index
        name_idx      # name index
    )

def build_manifest():
    """Build binary XML manifest."""
    # Strings we need
    strings = [
        "com.example.uncertaintyengine",
        "UncertaintyEngine",
        ".MainActivity",
        "MAIN",
        "LAUNCHER",
        "android.intent.action",
        "android.intent.category",
        "permission.INTERNET",
    ]

    str_data, smap = encode_string_pool(strings)

    # Build XML structure
    xml_chunks = bytearray()

    # Start document
    xml_chunks += struct.pack('<HHHHII', 0x0003, 1, 0, 0, 0, 0)

    # Root manifest element
    pkg_name_idx = smap["com.example.uncertaintyengine"]
    xml_chunks += encode_start_tag(pkg_name_idx, [
        (0xFF, smap["permission.INTERNET"], 0, 0x12000000, 0)  # uses-permission
    ])

    # Application element
    app_label_idx = smap["UncertaintyEngine"]
    xml_chunks += encode_start_tag(app_label_idx)

    # Activity element
    activity_name_idx = smap[".MainActivity"]
    xml_chunks += encode_start_tag(activity_name_idx)

    # Intent filter
    xml_chunks += encode_start_tag(0)  # empty string for action

    # Action MAIN
    action_idx = smap["MAIN"]
    xml_chunks += encode_start_tag(action_idx)
    xml_chunks += encode_end_tag(action_idx)

    # Category LAUNCHER
    category_idx = smap["LAUNCHER"]
    cat_ns_idx = smap["android.intent.category"]
    xml_chunks += encode_start_tag(category_idx)
    xml_chunks += encode_end_tag(category_idx)

    # Close intent-filter
    xml_chunks += encode_end_tag(0)

    # Close activity
    xml_chunks += encode_end_tag(activity_name_idx)

    # Close application
    xml_chunks += encode_end_tag(app_label_idx)

    # Close manifest
    xml_chunks += encode_end_tag(pkg_name_idx)

    # End document
    xml_chunks += struct.pack('<HHHH', 0x0003, 0, 0, 0)

    result = bytes(xml_chunks)

    # Add resource table placeholder
    res_table = struct.pack('<HHIII', 0x0008, 0, 0, 0, 0)
    result = result + res_table

    return str_data + result

# Test
manifest_xml = build_manifest()
print(f"Manifest binary size: {len(manifest_xml)} bytes")

# Check APK parsing
try:
    import zipfile
    apk_path = f"{OUT}/apk/app-unsigned.apk"

    # Remove old APK and recreate
    if os.path.exists(apk_path):
        os.remove(apk_path)

    with zipfile.ZipFile(apk_path, 'w', zipfile.ZIP_DEFLATED) as apk:
        apk.write(f"{OUT}/dex/classes.dex", "classes.dex")
        apk.writestr("AndroidManifest.xml", manifest_xml)

        # Copy assets
        assets_dir = f"{APP_DIR}/src/main/assets/public"
        if os.path.exists(assets_dir):
            for root, dirs, files in os.walk(assets_dir):
                for f in files:
                    full_path = os.path.join(root, f)
                    arc_path = 'assets/' + os.path.relpath(full_path, assets_dir)
                    apk.write(full_path, arc_path)

    print(f"Created unsigned APK: {os.path.getsize(apk_path)} bytes")
except Exception as e:
    print(f"Error creating APK: {e}")
    raise
