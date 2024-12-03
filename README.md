![coreply banner](./docs/static/coreply_banner.svg)

**coreply** is an open-source Android app designed to make texting faster and smarter by providing AI-generated auto-fill suggestions while you type. Whether you're replying to friends, family, or colleagues, coreply enhances your typing experience with intelligent, context-aware suggestions.

## Features

![coreply demo](./docs/static/coreply_demo.gif)

-   **Real-time AI Suggestions**: Get accurate, context-aware suggestions as you type.
-   **Customizable LLM Settings**: Supports any inference service having a OpenAI compatible API.
-   **No Data Collection**: All traffic goes directly to the inference API. No data passes through intermediate servers.

## Supported Texting Apps

-   WhatsApp
-   Instagram
-   Tinder
-   Hinge

## Getting Started

### Prerequisites

-   Device running Android 13 or higher
-   API key for an OpenAI-compatible inference service

### Installation & Usage

1. Download the latest APK from the [releases page](https://github.com/coreply/coreply/releases)
2. Install the APK on your Android device.
3. Setup the app with your API key, baseURL (if not using OpenAI) and model name.
4. Toggle the switch and grant necessary permissions to enable the service.
5. Start typing in your messaging app, and see suggestions appear! Single tap on the suggestion to insert one word, or long press to insert the entire suggestion.

### Build From Source

1. Clone the repository:
2. Open the project in Android Studio.
3. Sync the Gradle files and resolve any dependencies.
4. Build and run the app on your preferred device or emulator.

## Suggestions on Model Selection

-   Small models having ~2B parameters struggle to understand what's going on. They are not able to provide good suggestions.
-   Medium models such as the 8B version of llama 3.1 and the 9B version of Gemma 2 are cost effective and provide good suggestions.
-   Larger models like the 70B version of llama 3.1 provides even better suggestions but are more expensive.
-   Gemma 2's non-English abilities seems slightly better the llama 3 family, but llama 3.1 has slightly better English abilities based on my testing.
-   GPT-4o and xAI's Grok provides really good suggestions but beware of the cost.

## Contributing

All contributions are welcome! However, the code was based on an old project in 2016, so please be patient with the code quality and expect major architectural changes in the future.

## Roadmap

### Planned Features

-   Hosted version
-   Support for more messaging apps

### Known Issues

-   The app cannot read images, videos, voice notes, or other non-text content. Contextual suggestions may be limited in these cases.
-   Autofill suggestions in WhatsApp on a non-English locale may include extra words. This is a known issue and will be fixed in a future release.
-   Banking apps in asia commonly block apps from unknown sources having accessibility services permission due to security reasons. If you are facing this issue, you can setup [pyan accessibility shortcut](https://support.google.com/accessibility/android/answer/7650693?hl=en#step_1) to toggle the coreply on/off quickly. In the future there might be a Play Store listing to avoid this issue.

## License

coreply

Copyright (C) 2024 coreply

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
