# Project Plan

A PDF utility app named RTPC. Features: Camera to PDF (scanning), Images to PDF, PDF Merger, and other PDF utilities. The app should follow Material Design 3, have a vibrant energy, and support edge-to-edge display.

## Project Brief

# Project Brief: RTPC - PDF Utility Suite

RTPC is a high-energy, Material Design 3-compliant utility application designed to streamline PDF document management on Android. It provides users with a vibrant and intuitive interface to create, merge, and manage PDF documents using modern Android capabilities.

### Features
*   **Camera to PDF (Document Scanning):** Utilize the device camera to capture document images, perform basic edge detection/cropping, and compile them into a high-quality PDF.
*   **Images to PDF Converter:** Select multiple images from the device gallery and convert them into a single, organized PDF file.
*   **PDF Merger:** Efficiently combine two or more existing PDF files into a single document for better file management.
*   **PDF Document Manager:** A central hub to view, share, and delete generated PDF files, utilizing a modern, edge-to-edge list interface.

### High-Level Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material Design 3 (M3)
*   **Navigation:** **Jetpack Navigation 3** (state-driven approach)
*   **Adaptive Layout:** **Compose Material Adaptive** library for optimized experiences across mobile and large-screen devices.
*   **Asynchronous Processing:** Kotlin Coroutines & Flow
*   **Camera Integration:** CameraX for document capture
*   **Image Loading:** Coil (Compose-optimized)
*   **PDF Manipulation:** Android `PdfDocument` API or lightweight PDF library integration.

## Implementation Steps

### Task_1_Setup_Navigation_Theme: Set up the project foundation including Material 3 theme with vibrant colors, edge-to-edge display, and Navigation 3 architecture.
- **Status:** COMPLETED
- **Updates:** Implemented Material 3 theme with vibrant colors, enabled edge-to-edge display, and set up Navigation 3 with an adaptive layout. Created a Home screen with feature cards and configured an adaptive app icon. Project successfully builds on SDK 37.
- **Acceptance Criteria:**
  - Material 3 theme with vibrant light/dark color schemes implemented
  - Edge-to-edge display enabled
  - Navigation 3 set up with Home screen and adaptive layout
  - Project builds successfully

### Task_2_PDF_Creation_Scanning: Implement Camera to PDF (CameraX) and Images to PDF features using the PdfDocument API.
- **Status:** COMPLETED
- **Updates:** Implemented Camera to PDF using CameraX with a multi-page capture UI. Implemented Images to PDF conversion using the Android Photo Picker. PDF generation is handled by a common PdfGenerator utility using PdfDocument API. Files are saved to the app's external documents directory. Integrated both screens into the Navigation 3 setup.
- **Acceptance Criteria:**
  - CameraX integration captures images and converts them to PDF
  - Gallery image selection and PDF conversion functional
  - PDF files are saved correctly to local storage

### Task_3_PDF_Management_Merging: Implement PDF Merger utility and the Document Manager screen for viewing and managing generated files.
- **Status:** COMPLETED
- **Updates:** Implemented PDF Merger utility using pdfbox-android, allowing merging of multiple PDFs while preserving text searchability. Developed the Document Manager screen to list, share, and delete PDFs, with FileProvider integration for secure sharing. Full navigation integration completed.
- **Acceptance Criteria:**
  - Multiple PDFs can be merged into a single file
  - Document Manager displays a list of PDFs with Share and Delete actions
  - UI follows Material 3 guidelines and is adaptive

### Task_4_Final_Polish_Verification: Create an adaptive app icon, refine the UI aesthetics, and perform a final verification of the application.
- **Status:** COMPLETED
- **Updates:** Completed final UI polish and verification. Added a "Grant Permission" button in the Camera Scan screen to handle permission denials gracefully. Ensured all features (Scan, Convert, Merge, Manage) are stable and follow Material 3 guidelines. Final build is successful.
- **Acceptance Criteria:**
  - Adaptive app icon implemented
  - Vibrant and energetic Material 3 UI polish applied
  - Application is stable with no crashes
  - All features (Scan, Convert, Merge, Manage) verified
- **Duration:** N/A

