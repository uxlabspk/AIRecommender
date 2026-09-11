<div align="center">

# AI Recommender

### Your Perfect Outfit Is Just A Tap Away!

An **AI-powered fashion and makeup recommendation** Android app. Get personalized outfit and makeup suggestions based on your body type, style preferences, skin tone, occasion, and budget — all visualized as photorealistic AI-generated images.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=openjdk&logoColor=white)](https://www.java.com/) [![Android](https://img.shields.io/badge/Android-Min%2023-green?style=flat-square&logo=android&logoColor=white)](https://developer.android.com) [![Firebase](https://img.shields.io/badge/Firebase-Auth%20%26%20Firestore-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com/) [![Hugging Face](https://img.shields.io/badge/Hugging%20Face-Flux%20LoRA-FFD21E?style=flat-square&logo=huggingface&logoColor=black)](https://huggingface.co/) [![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

</div>

---

## Why AI Recommender?

Most fashion apps give you text-based suggestions or generic templates. AI Recommender generates **photorealistic images** of outfits and makeup looks tailored specifically to **your** body, skin, and style — so you can see exactly how something will look before you buy it.

> "Don't guess how an outfit will look. Let AI show you."

---

## Features

### Cloth Recommender

Input your age, gender, body type, height, weight, size, style preference, colors, fabric, occasion, and budget — and get a full-body photorealistic image of a model wearing a personalized outfit, with traditional Pakistani-Islamic fashion aesthetics.

### Makeup Recommender

Specify your skin tone, skin type, makeup style, shades, and accessories to receive a professional beauty portrait showcasing a complete makeup look tailored to your features and occasion.

### Complete Fashion Style

Get a **full head-to-toe look** combining clothing, makeup, shades, and accessories in one generation — the ultimate style recommendation for any occasion.

### Save to Favourites

Love a recommendation? Save it to your favourites collection, stored in Firebase Firestore, and access it anytime from your profile.

### Download to Gallery

One-tap download of any generated image directly to your device's gallery.

### User Authentication

Sign up with email/password or continue with Google. Your profile, favourites, and preferences are tied to your account.

### And more

- **14 input parameters** for cloth recommendations — granular control over every detail
- **8 input parameters** for makeup recommendations — skin, style, and occasion-aware
- **19 input parameters** for the complete fashion style — the most comprehensive recommendation
- **Real-time image generation** via Hugging Face Flux LoRA API
- **Firebase integration** — auth, Firestore, and storage
- **Clean Material Design UI** with bottom navigation and view binding

---

## Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 35 (compileSdk), minSdk 23
- A [Hugging Face](https://huggingface.co/) API key (for Flux LoRA endpoint)
- A [Firebase](https://firebase.google.com/) project with Auth, Firestore, and Storage enabled
- `google-services.json` placed in `app/`

### Run it

```bash
git clone https://github.com/uxlabspk/AIRecommender.git
cd AIRecommender
```

1. Place your `google-services.json` in the `app/` directory.
2. Add your API keys to `gradle.properties`:

```properties
API_KEY=your_huggingface_api_key
SupaBaseApiKey=your_supabase_api_key
```

3. Build and run:

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run on an emulator or device.

---

## How it works

```
User fills form (body type, style, occasion, budget, etc.)
    ↓
Prompt Builder       Constructs a detailed photorealistic prompt from inputs
    ↓
Image Request        POST to Hugging Face Flux LoRA endpoint
    ↓
Response Parser      Extracts image URL from JSON response
    ↓
Image Display        ResultActivity loads the generated image via Glide
    ↓
Save / Download      Favourite to Firebase Firestore or save to device gallery
```

---

## Tech Stack

| Layer | Tech |
|-------|------|
| Language | **Java** — Android native |
| Auth | **Firebase Auth** — email/password + Google Sign-In |
| Database | **Firebase Firestore** — user profiles, favourites |
| Storage | **Firebase Storage** — uploaded images |
| Image Gen | **Hugging Face Flux LoRA** — photorealistic image generation |
| Image Loading | **Glide** — efficient image loading and caching |
| UI | **Material Design** — bottom nav, view binding, constraint layout |
| Networking | **OkHttp + Retrofit** — HTTP client for API calls |

---

## Project Structure

```
AIRecommender/
├── app/src/main/java/io/github/uxlabspk/airecommender/
│   ├── view/                  Activities & fragments
│   │   ├── IntroductionActivity.java   Onboarding / entry
│   │   ├── LoginActivity.java          Email login
│   │   ├── SignupActivity.java         Email signup
│   │   ├── MainActivity.java           Home + profile tabs
│   │   ├── ClothFormActivity.java      Cloth recommendation form
│   │   ├── MakeupActivity.java         Makeup recommendation form
│   │   ├── FashionStyleActivity.java   Complete look form
│   │   ├── ResultActivity.java         Cloth/makeup result view
│   │   ├── Fashion_results.java        Complete look result view
│   │   ├── FavouriteImagesActivity.java Saved favourites
│   │   ├── EditProfile.java            Profile editor
│   │   ├── AccountActivity.java        Account settings
│   │   └── Adapters/                   RecyclerView adapters
│   ├── viewmodel/             MVVM ViewModels
│   ├── repository/            Data repositories
│   ├── model/                 Data models (UserModel, ImageModel)
│   ├── api/                   ImageRequest, SupabaseImageUploader
│   └── utils/                 ConfirmDialog, ProgressStatus
├── app/src/main/res/          Layouts, drawables, menus, values
├── app/build.gradle.kts       Module dependencies
├── build.gradle.kts           Project-level config
├── settings.gradle.kts        Project settings
└── gradle/libs.versions.toml  Version catalog
```

---

## Configuration

All settings are managed via `buildConfigField` in `app/build.gradle.kts` and `gradle.properties`:

| Setting | Default | Description |
|---------|---------|-------------|
| `API_KEY` | (empty) | Hugging Face API key |
| `SupaBaseApiKey` | (empty) | Supabase API key |
| `compileSdk` | 35 | Android compile SDK |
| `minSdk` | 23 | Minimum Android version |
| `targetSdk` | 34 | Target Android version |

---

## Contributing

Contributions welcome.

1. Fork it
2. Create a branch (`git checkout -b feat/my-thing`)
3. Commit (`git commit -m 'Add my thing'`)
4. Push (`git push origin feat/my-thing`)
5. Open a PR

---

## License

MIT — do whatever you want with it.

---

**If AI Recommender saves you from another "what should I wear" moment, give it a star.**

It helps others find it, and tells me this is worth continuing.

[⭐ Star this repo](https://github.com/uxlabspk/AIRecommender/stargazers)
