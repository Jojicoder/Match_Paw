# Match Paw 🐾

An iOS app for browsing shelter animals and submitting adoption applications.

## Features

- **Browse Animals** — View available animals in a photo grid with species filters
- **Search** — Search by name, breed, or species
- **Animal Detail** — Full profile with photo, health status, and background notes
- **Adoption Applications** — Submit an application for any available animal
- **Application Tracking** — View the status of your submitted applications (Pending / Under Review / Approved / Rejected)
- **User Accounts** — Sign up, log in, and manage your profile

## Tech Stack

| Layer | Technology |
|---|---|
| iOS App | SwiftUI, Swift Concurrency (`async/await`) |
| Backend API | ASP.NET Core 9 (C#), hosted on Azure App Service |
| Database | TiDB (MySQL-compatible) |
| Image Storage | Azure Blob Storage |

## Architecture

```
Match_Paw/
├── Models/          # Animal, Applicant, AdoptionApplication
├── Services/        # APIService (all network calls)
├── ViewModels/      # AnimalViewModel, ApplicationViewModel, AuthViewModel
└── Views/
    ├── Animals/     # HomeView, SearchView, AnimalCardView, AnimalDetailView
    ├── Applications/# MyApplicationsView, ApplicationDetailView, ApplicationFormView
    ├── Auth/        # LoginView, SignUpView
    └── Main/        # MainTabView, ProfileView, SplashView
```

## Backend API

Base URL: `https://matchpaw-api-gxd2eggnhdgefsej.eastus-01.azurewebsites.net/api`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/animals` | List all animals |
| GET | `/animals/:id` | Get animal by ID |
| POST | `/animals` | Create animal |
| PUT | `/animals/:id` | Update animal |
| PATCH | `/animals/:id/photo` | Update animal photo URL |
| DELETE | `/animals/:id` | Delete animal |
| POST | `/applicants/login` | Login |
| POST | `/applicants` | Sign up |
| GET | `/adoptionapplications` | List applications |
| POST | `/adoptionapplications` | Submit application |
| GET | `/blob/sas-url?fileName=` | Get Azure Blob SAS URL for upload |

## Getting Started

### Requirements

- Xcode 16+
- iOS 17+

### Run the App

1. Clone the repo
2. Open `Match_Paw.xcodeproj` in Xcode
3. Select a simulator or device
4. Press **Run** (⌘R)

No additional setup needed — the app connects to the hosted backend automatically.

## Demo Accounts

| Email | Password | Notes |
|---|---|---|
| `demo.new@matchpaw.com` | `Demo1234` | Fresh account, no applications |
| `demo.pending@matchpaw.com` | `Demo1234` | Has a pending application for Buddy |
