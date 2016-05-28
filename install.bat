@echo off
adb -d install -r app\app-release.apk
adb shell am start -n com.farmerbb.notepad/com.farmerbb.notepad.MainActivity