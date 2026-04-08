# USB Dongle Hash Generator
# PowerShell script to generate SHA-256 hash for USB dongle licensing

param(
    [Parameter(Mandatory=$false)]
    [string]$Serial = "",
    [Parameter(Mandatory=$false)]
    [string]$Vendor = "",
    [Parameter(Mandatory=$false)]
    [string]$Product = ""
)

Write-Host "🔐 USB Dongle Hash Generator" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# If no parameters provided, use default SanDisk USB
if ([string]::IsNullOrWhiteSpace($Serial)) {
    Write-Host "⚠️  No USB details provided. Using default SanDisk USB:" -ForegroundColor Yellow
    $Serial = "4C530000200311207422"
    $Vendor = "SANDISK"
    $Product = "CRUZER_BLADE"
    Write-Host ""
}

# Create fingerprint
$fingerprint = "$Serial|$Vendor|$Product"

# Calculate SHA-256 hash
$hash = [System.Security.Cryptography.SHA256]::Create().ComputeHash([System.Text.Encoding]::UTF8.GetBytes($fingerprint))
$hashString = [System.BitConverter]::ToString($hash).Replace("-", "").ToLower()

# Display results
Write-Host "📱 USB Details:" -ForegroundColor Green
Write-Host "   Serial:  $Serial"
Write-Host "   Vendor:  $Vendor"
Write-Host "   Product: $Product"
Write-Host ""

Write-Host "🔑 Raw Fingerprint:" -ForegroundColor Green
Write-Host "   $fingerprint"
Write-Host ""

Write-Host "🔐 SHA-256 Hash:" -ForegroundColor Green
Write-Host "   $hashString"
Write-Host ""

Write-Host "📋 Copy this into LicenseManager.kt:" -ForegroundColor Cyan
Write-Host "   private const val MASTER_USB_HASH = `"$hashString`""
Write-Host ""

# Ask if user wants to save to file
$save = Read-Host "Save hash to file? (y/n)"
if ($save -eq "y" -or $save -eq "Y") {
    $output = @"
USB Dongle License Hash
Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

Serial:  $Serial
Vendor:  $Vendor
Product: $Product

Raw Fingerprint: $fingerprint
SHA-256 Hash:    $hashString

Paste into LicenseManager.kt:
private const val MASTER_USB_HASH = "$hashString"
"@
    
    $filename = "usb_hash_$Serial.txt"
    $output | Out-File -FilePath $filename -Encoding UTF8
    Write-Host "✅ Saved to $filename" -ForegroundColor Green
}

Write-Host ""
Write-Host "Done! 🎉" -ForegroundColor Green

<#
.SYNOPSIS
    Generates SHA-256 hash for USB dongle licensing

.DESCRIPTION
    This script creates a SHA-256 hash from USB device details (Serial, Vendor, Product)
    for use in the USB Dongle License Protection system.

.PARAMETER Serial
    USB device serial number

.PARAMETER Vendor
    USB device vendor/manufacturer name

.PARAMETER Product
    USB device product name

.EXAMPLE
    .\generate_usb_hash.ps1
    Uses default SanDisk USB details

.EXAMPLE
    .\generate_usb_hash.ps1 -Serial "ABC123XYZ" -Vendor "KINGSTON" -Product "DATATRAVELER"
    Generates hash for custom USB device

.NOTES
    Author: USB License Protection System
    Date: January 16, 2026
#>



