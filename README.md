![Token Android Cover](https://raw.githubusercontent.com/toshiapp/toshi-android-client/master/images/header.png)

Getting Started
===============

1. Clone this code to your machine
2. Download and fully install [Android Studio](https://developer.android.com/studio/index.html)
3. Open this code in Android Studio (Choosing the containing directory in Android Studio `Open` dialog works)
4. Click the `Play` button (`[Ctrl]` + `[Alt]` + `R`)

Running the linter
==================

[ktlint](https://github.com/shyiko/ktlint/) is used to check the format of the Kotlin code. It requires that wildcard imports are disabled in Android Studio.

To disable wildcard imports:

- Go to `Preferences... -> Editor -> Code Style -> Kotlin`
- open `Imports` tab, select all `Use single name import` options and remove `import java.util.*` from `Packages to Use Import with '*'`.

The linter an be run via gradle task `check` or `ktlint`

Adding a dependency
===================

The plugin [gradle-witness](https://github.com/WhisperSystems/gradle-witness) is used to verify dependencies. You will
need to add the relevant checksums to build.gradle. More information can be found on the gradle-witness project.

Making a new build for Play store (Dev only)
============================================

1. Open app/build.gradle
2. Increase the versionCode by 1.
3. Optionally change the versionName to whatever you want.
4. Build the APK however you want (in Android Studio or via gradle)
    - The APK is split so you will end up with multiple APKs. Gradle takes care of numbering them correctly.
    - You'll need the release certificate, alias and password.
5. Commit the changes to build.gradle, upload the APKs to Play store


<a href='https://play.google.com/store/apps/details?id=org.toshi&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='220px'/></a>



```
	Copyright (c) 2017 Toshi Inc

	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
