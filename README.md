# AK Finance AI v1.1 MVP

Android MVP for local SMS financial intelligence.

Features:
- New SMS reception
- Sender ID analysis before message analysis
- Bank / OTP / transaction / alert / unknown classification
- Non-bank labeling
- Amount extraction
- OTP masking for training data
- User confirmation/correction
- Local SQLite storage
- Conservative transaction creation
- GitHub Actions APK build without Android Studio or Gradle Wrapper

Important: OTP values are not stored in the sanitized training text.
