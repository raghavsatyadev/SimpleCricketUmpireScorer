# Skill: format-kotlin

## Description

Applies the official Google Internal Formatting (ktfmt) to Kotlin source files using a local Windows
batch wrapper.

## When to use

- IMMEDIATELY after writing or editing any `.kt` file.
- BEFORE responding to the user with the final code block.

## Arguments

- `file_path`: The relative path to the Kotlin file (e.g., `src\main\java\com\myapp\Main.kt`).

## Execution Step

Execute the following command in the terminal:

```shell
ktfmt "{file_path}"
```