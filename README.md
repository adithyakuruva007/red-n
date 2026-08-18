# Red AI Agent
This is an experimental fork of original red project
Red is a private, powerful AI Agent built with Kotlin Multiplatform and Compose Multiplatform. It runs natively on Desktop (Linux, macOS, Windows) and Mobile (Android, iOS).

**Creator & Maintainer:** [adithyakuruva007](https://github.com/adithyakuruva007)

## Key Features

- **Private On-Device Inference**: Run AI models offline directly on your device using Google's LiteRT LM SDK (Gemma 2B/4B).
- **Multi-Service Integrations**: Seamlessly connect to multiple remote LLM providers (Google Gemini, Anthropic Claude, OpenAI, Ollama, etc.) with automatic fallback configuration.
- **Secure Sandbox Executions**: Android client ships with an embedded user-space Alpine Linux sandbox (`proot`) allowing the assistant to run real-world shell scripts safely.
- **Automated Standing Tasks**: Schedule recurring background cron tasks or on-heartbeat checks.
- **Interactive UI Renderers**: Supports dynamic UI components (`red-ui` fences) to render custom forms, choices, tables, and buttons directly inside the chat.

## Getting Started

### Prerequisites
- JDK 21
- Android SDK (for compiling the Android app)

### Building the Project

The project is structured as a Kotlin Multiplatform workspace.

#### Compile and run desktop app:
```bash
./gradlew :composeApp:run
```

#### Package desktop installer (Debian package):
```bash
./gradlew :composeApp:packageDeb
```

#### Compile debug Android APK:
```bash
./gradlew :androidApp:assembleFossDebug
```

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt) for more details.
