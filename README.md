# Graal Bridge

This mod requires any version of JsMacros and is currently in beta.

---

## Table of Contents

- [Features](#features)
    - [Mappings](#mappings)
    - [TypeScript Support](#typescript-support)
    - [Enhanced Imports](#enhanced-imports)
    - [Multithreading](#multithreading)
- [Installation](#installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### Mappings

Graal Bridge supports various mapping formats. Supported mappings include:

- Official
- Mojmap
- Srg
- Intermediary
- Yarn
- Hashed
- Quilt
- Custom mappings

Mappings must be placed in the `${mappings}/${mcVersion}` folder with the specified name and an optional file extension.
If no mappings are found, they will be automatically downloaded.

---

### TypeScript Support

Graal Bridge provides seamless support for TypeScript.
Files with a `.ts` extension are transpiled automatically.

```ts
function add(a: number, b: number): number {
    return a + b;
}

Chat.log(add(1, 2));
```

When importing other TypeScript files, the file extension must be specified.

```ts
import {add, sub} from "./math.ts";
```

---

### Enhanced Imports

Simplify your code with advanced import capabilities:

1. **Multiple Class Imports**: Import multiple classes from a package, including mapped classes.

```ts
import {AzaleaBlock, AnvilBlock, BellBlock} from "net.minecraft.block";
```

2. **Import Aliases**: Assign custom names to imported classes.

```ts
import {class_2199 as AnvilBlock, class_3709 as BellBlock} from "net.minecraft";
```

3. **Static Value Imports**: Import static values directly from a class.

```ts
import {err, out} from "java.lang.System";
```

---

### Multithreading

Unlock multithreading capabilities in GraalJS for better performance. Note that this feature may cause instability in
some cases.

```ts
Graal.setMultithreading(true);
new Thread(() => {
    Chat.log("Hello there");
}).start();
```

---

## Installation

1. Ensure you have any version JsMacros installed.
2. Download the Kotlin library of the mod loader.
2. Download the latest version of Graal Bridge from
   the [release page]("https://github.com/Etheradon/GraalBridge/releases").
3. Place the downloaded file into the mods folder of your Minecraft instance.

---

## Usage

### Setting Up Mappings

1. Place your mappings in the `${mappings}/${mcVersion}` folder.
2. Use the `Graal.loadMappings()` method to load the desired mapping.

### Working with TypeScript

- Create `.ts` files for your scripts.
- Include them in your project, ensuring all imports use the `.ts` extension.

### Enabling Multithreading

- Call `Graal.setMultithreading(true)` to enable multithreading in your scripts.

---

## Roadmap

- **Java 8 Support**: Compatibility for older Java versions.
- **Forge Compatibility**: Fix issues with class mapping functionality in Forge.
- **Enhanced Mapping Options**:
    - Specify versions for mappings.
    - Support for custom mappings.
- Remove JsMacros dependency.

---

## Contributing

Contributions are welcome! To get started:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature-name`.
3. Commit your changes: `git commit -m "Add new feature"`.
4. Push to the branch: `git push origin feature-name`.
5. Open a pull request.

Make sure you are using Java 21 and that you are also using Java 21 for Gradle.
Intellij: File | Settings | Build, Execution, Deployment | Build Tools | Gradle

---

## License

This project is licensed under the [GPLv3](LICENSE).
