# Nikhil Fuel App – Standalone Android App

This project packages the Nikhil Fuel Services HTML app as a standalone Android application.

## App behavior
- No browser/address/URL bar.
- No browser navigation controls.
- Opens directly into the fuel-management app.
- Runs offline; the app itself does not require Internet permission.
- Nikhil Petroleum (NP) and Nikhil Fuel Services (NFS) use separate local data keys.
- Full-screen presentation for a clean tablet/phone app experience.

## Build without Android Studio
Use the included GitHub Actions workflow. Upload the project to GitHub, open **Actions**, select **Build APK**, and run the workflow. The generated APK is available as a workflow artifact.
