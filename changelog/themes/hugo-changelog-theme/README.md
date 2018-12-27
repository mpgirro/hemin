Hugo Changelog Theme
=====

A [Hugo](https://gohugo.io/) theme to display a changelog

# Features
 - Build with [Spectre.css](https://picturepan2.github.io/spectre/) (All unused components are disabled)
 - Pagination
 - Mobile friendly

# Workflow

## Conventions
 - Create non-released entries in `experimental/` folder. All of them are displayed in the top of the first page
 - Create released entries in `released/` folders. Entries are sorted by Weight. Weight is displayed as version
 - Your hugo changelog website is located in `site/changelog/` folder
 - You are using `master` branch as the stable branch and `develop` branch as a working branch (needed for the post-merge webhook only)

## Scripts
 - `Makefile` - list of useful commands
 - `release.py` - moves changes from the `experimental/` folder to the `released/` folder, assigns version number and generates release-commit
 - `.githooks/post-merge` - verifies that `experimental/` folder is empty during the merge from the working branch to the stable branch

## Dependencies
 - For release script:
   ```bash
    sudo pip install python-frontmatter
   ```

## Description
 1. When a pull request is ready, a developer creates a changelog entry:
    ```bash
    make change
    ```
    The command creates a *.md file with random name (ensures that there will be no merge conflicts) in `site/changelog/content/experimental/` folder

 2. The developer updates created file with changes. Changes are going to be rendered in the `experimental` section of the template

 3. Preview the site with the command:
    ```bash
    make serve_changelog
    ```

 4. When the working branch is ready to be merged in the stable branch, the developer runs:
    ```bash
    make release
    ```
    The command will move all *.md files from the `experimental/` folder to the `released/` folder, assign the release version and generate the commit with related changes

 5. The developer merges working branch in to the stable branch


# Shortcodes
 - `{{% tag fixed %}}` - create a specific tag before entry text. Available tag types are: added, changed, fixed, deprecated, removed, security

# Settings
```
[params]
    customCSS = ["css/styles.css"]  # List of css files to include on the website. Relative to the static/ folder
```

# Development
## Serve example website
```bash
cd exampleSite && hugo serve --theme hugo-changelog-theme --themesDir ../../ --baseURL http://localhost/
```
