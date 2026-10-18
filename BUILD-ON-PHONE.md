# Build ND FPS Boost on your phone (no Termux)

1. Open https://github.com/ and sign in.
2. Tap **+** then **New repository**. Name it `nd-fps-boost`, set it to **Private** or **Public**, and create it.
3. On the new repository page, choose **Add file → Upload files**.
4. Extract this ZIP using your phone's Files app first. Upload the *contents* of the `nd-fps-boost` folder, including the hidden `.github/workflows/build.yml` file. If your phone's file picker hides `.github`, use GitHub's web editor to create `.github/workflows/build.yml` and paste the workflow content from this project's file.
5. Commit the uploaded files to `main`.
6. Open the repository's **Actions** tab, select **Build ND FPS Boost**, and tap **Run workflow** if it has not started automatically.
7. Open the finished workflow run. Under **Artifacts**, download `nd-fps-boost-jar` and extract it. The `.jar` is the build output.

Important: a successful compilation does not prove the mod works in Minecraft 26.3 or Mojo's LTW renderer. This source is an unverified prototype; if the build fails, open the run and read the first red error. Do not install a JAR unless the build succeeds, and back up your game/world before testing mods.
