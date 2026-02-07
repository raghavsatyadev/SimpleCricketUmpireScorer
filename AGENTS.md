# Agent Guidelines

## Code Style & Formatting

1. **Strict Prohibition on Manual Formatting:**
    - You are PROHIBITED from manually adjusting whitespace, indentation, or line breaks to "fix"
      style.
    - Do not attempt to emulate Google Internal Formatting yourself. You will get it wrong.

2. **Mandatory Tool Usage:**
    - The ONLY way to format Kotlin code is by invoking the `format-kotlin` skill.
    - You must run this skill on every `.kt` file you modify before presenting the code to the user.

3. **Trust the Tool:**
    - Once the `ktfmt.bat` script has run, the code is considered perfect. Do not make subsequent "
      touch-ups."