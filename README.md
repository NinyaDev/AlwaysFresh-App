# AlwaysFresh

### A native Android app built for a Mobile Development class, designed to help users track fridge inventory and reduce food waste.

**AlwaysFresh** is a smart digital fridge inventory tracker that classifies items by freshness, doubles as an auto-generated shopping list, and visualizes waste patterns over time. This project is a demonstration of modern Android architecture using Kotlin, MVVM, Room persistence, and reactive UI with Kotlin Flow.

---

## Demo

https://github.com/user-attachments/assets/02071409-9963-45cb-94e1-47e085d1f766

<div align="center">
<img src="https://github.com/user-attachments/assets/c8594dc2-ba36-473d-8187-5c691dc921dc" alt="AlwaysFresh Screenshot 1" width="300">
<img src="https://github.com/user-attachments/assets/8a5390ce-f74b-4bc7-b4b4-ed27edcee2ef" alt="AlwaysFresh Screenshot 2" width="300">
</div>

---
## Features

This application was designed around one question: *"what's about to go bad, and what do I need to replace?"*

| Feature | Description |
| :--- | :--- |
| **Inventory Tracking** | Add, edit, and remove food items with expiration dates, stored in a local Room database. |
| **Freshness Classification** | Each item is color-coded as **Fresh**, **Expiring Soon** (within 7 days), or **Expired**, computed live from the expiration date. |
| **Auto Shopping List** | Items you remove from inventory are soft-deleted and automatically populate a Shopping List — the things you just ran out of. |
| **Waste Analytics** | A dashboard summarizing how many items you consumed vs. let expire, to surface waste patterns. |
| **Dark Mode** | Per-user theme preference stored in SharedPreferences and applied at launch. |
| **Responsive Layouts** | Every screen has a dedicated landscape variant. The main screen swaps a BottomNavigationView (portrait) for a NavigationRail (landscape). |

---

## How to Run

This is an Android application, so the recommended workflow is through **Android Studio** with the Gradle Wrapper handling all dependencies automatically.

### Prerequisites

- **Android Studio** (Giraffe or newer recommended)
- **Android SDK 36** with build-tools installed
- An **Android emulator** (API 35+) or a physical device in developer mode

### Steps

1.  Clone the repository:
    ```bash
    git clone https://github.com/NinyaDev/AlwaysFresh-App.git
    ```
2.  Open **Android Studio** → `File` → `Open` → select the `AlwaysFresh-App` folder.
3.  Wait for the initial **Gradle Sync** to complete — this will auto-generate a fresh `local.properties` pointing to your local Android SDK and download all dependencies defined in `gradle/libs.versions.toml`.
4.  Select an emulator (or connected device) from the device selector.
5.  Click the **Run ▶** button (or `Shift + F10`).

<!-- Common issue when cloning on a fresh machine -->
**Troubleshooting:**
If Gradle Sync fails with an SDK location error, go to `File` → `Project Structure` → `SDK Location` and confirm the Android SDK path is set. Android Studio will rewrite `local.properties` automatically after this.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **Architecture** | MVVM (AndroidViewModel + LiveData + Kotlin Flow) |
| **UI** | Material Components, ConstraintLayout, ViewBinding |
| **Persistence** | Room (SQLite) with KSP annotation processing |
| **Navigation** | Fragments, ViewPager2, BottomNavigationView / NavigationRail |
| **Build** | Gradle Kotlin DSL, Version Catalog (`libs.versions.toml`) |

---

## Project Structure
```
AlwaysFresh-App/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/alwaysfresh/
│   │   │   ├── data/             # Room entities, DAOs, AppDatabase
│   │   │   ├── model/            # Repository + domain logic (freshness classification)
│   │   │   ├── viewmodel/        # Shared MainViewModel (activityViewModels)
│   │   │   ├── adapter/          # RecyclerView + ViewPager2 adapters
│   │   │   ├── fragment/         # Inventory, Dashboard, ShoppingList, WasteAnalytics
│   │   │   └── *Activity.kt      # MainActivity, AddItem, ItemDetail, Settings
│   │   └── res/
│   │       ├── layout/           # Portrait layouts
│   │       ├── layout-land/      # Landscape variants
│   │       ├── values/           # Strings, colors, themes (light)
│   │       └── values-night/     # Dark mode overrides
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml        # Dependency version catalog
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore                    # Files and folders to be excluded from version control
└── README.md
```

---

## License

This project is distributed under the **MIT License**. See the `LICENSE` file for more details.

---

## Contact

**Adrian Ninanya**

* **GitHub**: [NinyaDev](https://github.com/NinyaDev)
* **Linkedin**: [Adrian Ninanya](https://www.linkedin.com/in/adrian-ninanya/)
* **Project Link**: [https://github.com/NinyaDev/AlwaysFresh-App](https://github.com/NinyaDev/AlwaysFresh-App)
