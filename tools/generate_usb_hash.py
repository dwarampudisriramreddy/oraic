#!/usr/bin/env python3
"""
USB Dongle Hash Generator
Python script to generate SHA-256 hash for USB dongle licensing
"""

import hashlib
import sys
from datetime import datetime

def generate_hash(serial: str, vendor: str, product: str) -> tuple[str, str]:
    """
    Generate USB fingerprint and SHA-256 hash
    
    Args:
        serial: USB device serial number
        vendor: USB device vendor/manufacturer
        product: USB device product name
        
    Returns:
        Tuple of (fingerprint, hash)
    """
    fingerprint = f"{serial}|{vendor}|{product}"
    hash_value = hashlib.sha256(fingerprint.encode()).hexdigest()
    return fingerprint, hash_value

def main():
    print("🔐 USB Dongle Hash Generator")
    print("=" * 40)
    print()
    
    # Check if arguments provided
    if len(sys.argv) == 4:
        serial = sys.argv[1]
        vendor = sys.argv[2]
        product = sys.argv[3]
        print("✅ Using provided USB details:")
    else:
        print("⚠️  No USB details provided. Using default SanDisk USB:")
        serial = "4C530000200311207422"
        vendor = "SANDISK"
        product = "CRUZER_BLADE"
    
    print()
    
    # Generate hash
    fingerprint, hash_value = generate_hash(serial, vendor, product)
    
    # Display results
    print("📱 USB Details:")
    print(f"   Serial:  {serial}")
    print(f"   Vendor:  {vendor}")
    print(f"   Product: {product}")
    print()
    
    print("🔑 Raw Fingerprint:")
    print(f"   {fingerprint}")
    print()
    
    print("🔐 SHA-256 Hash:")
    print(f"   {hash_value}")
    print()
    
    print("📋 Copy this into LicenseManager.kt:")
    print(f'   private const val MASTER_USB_HASH = "{hash_value}"')
    print()
    
    # Ask to save to file
    try:
        save = input("Save hash to file? (y/n): ").strip().lower()
        if save == 'y':
            filename = f"usb_hash_{serial}.txt"
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(f"USB Dongle License Hash\n")
                f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
                f.write(f"Serial:  {serial}\n")
                f.write(f"Vendor:  {vendor}\n")
                f.write(f"Product: {product}\n\n")
                f.write(f"Raw Fingerprint: {fingerprint}\n")
                f.write(f"SHA-256 Hash:    {hash_value}\n\n")
                f.write(f"Paste into LicenseManager.kt:\n")
                f.write(f'private const val MASTER_USB_HASH = "{hash_value}"\n')
            print(f"✅ Saved to {filename}")
    except (EOFError, KeyboardInterrupt):
        print("\n⏭️  Skipped saving to file")
    
    print()
    print("Done! 🎉")

if __name__ == "__main__":
    print()
    if len(sys.argv) > 1 and sys.argv[1] in ['-h', '--help']:
        print("""
Usage:
    python generate_usb_hash.py                           # Use default SanDisk USB
    python generate_usb_hash.py <serial> <vendor> <product>  # Use custom USB
    
Examples:
    python generate_usb_hash.py
    python generate_usb_hash.py ABC123XYZ KINGSTON DATATRAVELER
    
Arguments:
    serial   - USB device serial number
    vendor   - USB device vendor/manufacturer name
    product  - USB device product name
        """)
        sys.exit(0)
    
    main()



