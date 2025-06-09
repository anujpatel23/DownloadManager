# Download Manager - Modern Edition

A modern, feature-rich download manager built with Java Swing, featuring a clean MVC architecture, real-time progress tracking, and advanced download management capabilities.

## 🚀 Features

### Core Functionality
- **Multi-threaded Downloads**: Concurrent downloads with configurable thread pool
- **Pause/Resume/Cancel**: Full control over download lifecycle
- **Real-time Progress Tracking**: Live progress bars with speed and ETA calculations
- **Download Queue Management**: Automatic queue processing with priority handling
- **Resume Capability**: Automatic resume of interrupted downloads
- **Error Handling & Retry**: Intelligent retry mechanism with exponential backoff

### Modern UI
- **Clean Interface**: Modern Swing UI with enhanced styling
- **Responsive Design**: Adaptive layout that works on different screen sizes
- **Real-time Updates**: Live progress updates without blocking the UI
- **Context Menus**: Right-click actions for quick access
- **Keyboard Shortcuts**: Efficient keyboard navigation
- **Status Bar**: Real-time statistics and download information

### Advanced Features
- **Settings Management**: Persistent configuration with user preferences
- **File Type Detection**: Automatic file type detection and naming
- **Unique File Names**: Automatic handling of duplicate file names
- **Download History**: Track completed and failed downloads
- **Speed Monitoring**: Real-time download speed calculation
- **Network Optimization**: Configurable timeouts and connection settings

## 🏗️ Architecture

The application follows a clean **Model-View-Controller (MVC)** pattern:

```
src/com/downloadmanager/
├── model/                  # Data models and business entities
│   ├── Download.java       # Main download entity with state management
│   ├── DownloadStatus.java # Download status enumeration
│   └── DownloadSettings.java # Application settings management
├── service/                # Business logic and services
│   ├── DownloadService.java # Main download management service
│   └── DownloadEngine.java  # Core download execution engine
├── controller/             # UI controllers and business logic coordination
│   └── MainController.java # Main application controller
├── view/                   # UI components and windows
│   ├── MainWindow.java     # Main application window
│   ├── SettingsDialog.java # Settings configuration dialog
│   └── components/         # Reusable UI components
│       ├── ToolBar.java    # Main toolbar with controls
│       ├── StatusBar.java  # Status and statistics bar
│       └── DownloadTablePanel.java # Download list table
├── util/                   # Utility classes
│   ├── FileUtils.java      # File operations and utilities
│   └── NetworkUtils.java   # Network and URL utilities
└── DownloadManagerApp.java # Main application entry point
```

## 🛠️ Technical Implementation

### Threading Model
- **ExecutorService**: Managed thread pool for download tasks
- **ScheduledExecutorService**: Periodic tasks for progress updates
- **SwingUtilities**: Proper EDT handling for UI updates
- **Atomic Operations**: Thread-safe state management

### Download Engine
- **HTTP Range Requests**: Support for resumable downloads
- **Buffered I/O**: Efficient file writing with configurable buffer sizes
- **Connection Management**: Proper resource cleanup and timeout handling
- **Progress Calculation**: Real-time speed and ETA computation

### UI Components
- **Custom Table Renderers**: Enhanced progress bars and status indicators
- **Event-Driven Updates**: Reactive UI updates based on download events
- **Modern Styling**: Enhanced Swing components with modern appearance
- **Responsive Layout**: Adaptive layouts for different window sizes

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- No external dependencies required (uses only standard Java libraries)

### Compilation and Running

#### Windows
```bash
# Compile and run
compile_and_run.bat

# Or manually:
javac -d "out/production" -cp "src" src/com/downloadmanager/**/*.java
java -cp "out/production" com.downloadmanager.DownloadManagerApp
```

#### Linux/macOS
```bash
# Make script executable
chmod +x compile_and_run.sh

# Compile and run
./compile_and_run.sh

# Or manually:
javac -d "out/production" -cp "src" src/com/downloadmanager/**/*.java
java -cp "out/production" com.downloadmanager.DownloadManagerApp
```

### Optional: Enhanced UI with FlatLaf
For a more modern look, you can add the FlatLaf library:

1. Download FlatLaf JAR from [GitHub](https://github.com/JFormDesigner/FlatLaf)
2. Add to classpath when compiling and running:
```bash
javac -cp "src:flatlaf.jar" -d "out/production" src/com/downloadmanager/**/*.java
java -cp "out/production:flatlaf.jar" com.downloadmanager.DownloadManagerApp
```

## 📖 Usage Guide

### Adding Downloads
1. **Toolbar Method**: Enter URL in the toolbar field and click "Add Download"
2. **Menu Method**: File → Add Download (Ctrl+N)
3. **Keyboard Shortcut**: Press Ctrl+N anywhere in the application

### Managing Downloads
- **Pause**: Select downloads and click "Pause" or right-click → Pause
- **Resume**: Select paused downloads and click "Resume"
- **Cancel**: Select downloads and click "Cancel" (will delete partial files)
- **Remove**: Remove completed downloads from the list
- **Clear Completed**: Remove all finished downloads at once

### Settings Configuration
Access via File → Settings to configure:
- **Download Location**: Choose where files are saved
- **Concurrent Downloads**: Number of simultaneous downloads (1-10)
- **Connection Timeout**: Network timeout settings
- **Retry Settings**: Automatic retry configuration
- **Resume Settings**: Enable/disable download resuming

### Keyboard Shortcuts
- `Ctrl+N`: Add new download
- `Ctrl+,`: Open settings
- `Ctrl+Q`: Exit application
- `Delete`: Remove selected downloads
- `Space`: Pause/Resume selected downloads

## 🔧 Configuration

### Settings File
Settings are automatically saved to `download_manager.properties` in the application directory.

### Default Configuration
```properties
download.path=~/Downloads
download.max.concurrent=3
connection.timeout=30000
read.timeout=60000
buffer.size=8192
max.retries=3
auto.retry=true
resume.downloads=true
```

## 🎨 UI Customization

### Themes
The application supports multiple look and feel options:
1. **FlatLaf** (recommended): Modern, flat design
2. **System**: Native OS appearance
3. **Default**: Standard Java Swing appearance

### Color Scheme
- **Primary**: Blue (#007BD3) for active downloads
- **Success**: Green (#28A745) for completed downloads
- **Warning**: Orange (#FF8C00) for paused downloads
- **Error**: Red (#DC3545) for failed downloads

## 🔍 Troubleshooting

### Common Issues

**Downloads not starting**
- Check internet connection
- Verify URL is accessible
- Check download directory permissions

**UI not updating**
- Ensure proper EDT usage in custom modifications
- Check for blocking operations on UI thread

**High memory usage**
- Reduce buffer size in settings
- Limit concurrent downloads
- Clear completed downloads regularly

### Debug Mode
Add `-Ddebug=true` to JVM arguments for verbose logging:
```bash
java -Ddebug=true -cp "out/production" com.downloadmanager.DownloadManagerApp
```

## 🤝 Contributing

### Code Style
- Follow Java naming conventions
- Use proper JavaDoc comments
- Maintain MVC separation
- Ensure thread safety for shared resources

### Adding Features
1. Create feature branch
2. Implement following MVC pattern
3. Add appropriate error handling
4. Update documentation
5. Test thoroughly

## 📄 License

This project is open source and available under the MIT License.

## 🙏 Acknowledgments

- Java Swing community for UI components
- FlatLaf project for modern look and feel
- Contributors and testers

---

**Built with ❤️ using Java and Swing**
