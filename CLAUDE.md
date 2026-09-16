# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Mirage is a Bukkit/Spigot/Paper Minecraft plugin (Java 11) that lets server admins define custom
"resource" blocks: a block that, when broken with the right tool and requirements met, drops
configured rewards/costs, runs commands, and then regenerates itself after a delay. It is one
module of the larger `magemonkey-parent` multi-module Maven project (parent repo is at `../`
relative to this directory), but this checkout is its own git repository tracking `dev`/`main`.

## Build

This module builds against a parent POM (`studio.magemonkey:magemonkey-parent`) that must be
available (either built locally via `mvn install` from the parent repo, or resolved from
`https://repo.travja.dev/{releases,snapshots}`).

```bash
mvn clean install        # build the plugin jar
mvn clean package         # build without installing to local repo
```

There is no test suite in this module (no `src/test`) and no lint/format tooling configured
locally — CI (`.github/workflows/maven.yml`, `devbuild.yml`, `release.yml`) delegates to shared
reusable workflows in `magemonkeystudio/.github`. `devbuild.yml` deploys on push to `dev`,
`release.yml` on push to `main`, both only when files under `src/` change.

## Architecture

**Entry point**: `Mirage` (`JavaPlugin`) in `onEnable()` wires up listeners, loads config/data,
detects soft-depends (WorldGuard, Towny, Factions, Residence, GriefPrevention, ProtectionStones,
GriefDefender, Lands), and starts the regeneration watchdog. `Mirage.getInstance()` is the global
singleton accessor used throughout (no DI).

**Config-driven "resource" model** (`config.yml` → `entries` list → `Data` → `DataEntry`):
- `Data` parses `entries` from config into a `Map<BlockType, DataEntry>` and separately persists
  in-progress regenerations to `regen.yml` via `RegenerationEntry`.
- `DataEntry` is the parsed representation of one config entry: which block(s) it matches, what
  it turns into on break (`break-material`), regen delay, allowed tools + tool-break message/damage,
  requirements to break it, rewards/costs on break, weighted `chance` map for what it regenerates
  into, and optional post-break commands.
- On `BlockBreakEvent` (`InteractListener`), the flow is: check disabled world/region → check
  protection plugin hooks (`isProtected`) → look up a matching `DataEntry` via `Data.match()` →
  validate tool → validate requirements → apply costs/rewards → damage the tool → cancel drops or
  let the block type handle its own break → place the `break-material` → register a
  `RegenerationEntry` that will restore the original block after `regen-delay` ticks.
- `Data.startRegenerationWatchdog()` runs a periodic async safety net that re-schedules any
  regeneration tasks that should have fired but didn't (e.g. server restarts), and
  `checkChunkRegeneration` re-arms tasks when a chunk holding a pending regen loads.

**Pluggable type hierarchies** — each has an abstract base class with a static `make(String id)`
factory that parses a prefixed config string, validates the referenced item/block exists in the
relevant plugin, and interns instances via a `canonize()` registry so equal ids share one instance:
- `blocks.BlockType` (`VanillaBlockType`, `OraxenBlockType`, `ItemsAdderBlockType`) — what a
  physical block is/becomes; `isInstance(Block)` matches, `place(Block)` sets it, `handleBreak`
  controls drop behavior.
- `tools.ToolType` (`VanillaToolType`, `SapphireToolType`, `OraxenToolType`, `ItemsAdderToolType`,
  `DivinityToolType`) — what counts as a valid tool for a `DataEntry`.
- `requirements.Requirement` (enchantment, Fabled skill/class, Jobs job, mcMMO skill, Fusion
  profession) — gating conditions checked against the breaking player.
- `rewards.Reward` (vanilla exp, Vault money, Jobs money/exp/points, Fabled skill/exp, mcMMO exp)
  — applied on break; `AmountReward` subclasses represent a numeric amount and are treated as a
  *cost* when the parsed amount is negative (split into `DataEntry.costs` vs `.rewards`).

Each factory logs and skips (returns `null`/throws, caught by the caller) rather than failing
config load entirely when a referenced integration plugin isn't enabled or an id is unknown —
config entries are defensively parsed field-by-field in `DataEntry`'s constructor for this reason.

**Protection-plugin hooks** (`hooks/`) are static wrapper classes (`WorldGuardHook`, `TownyHook`,
`FactionsUUIDHook`, `LandsHook`) queried from `InteractListener.isProtected()`; each corresponding
soft-depend plugin must be both installed and enabled in `config.yml` (see the booleans in
`Mirage.onEnable()`) before its hook is consulted. Other protection plugins (Residence,
GriefPrevention, ProtectionStones, GriefDefender) are checked inline in `isProtected()` without a
dedicated hook class.

**Crop/plant growth restriction**: `CropListener` + the `restrict*` static flags on `Data` (set
from `disable_crop_growth`/`disable_melon_growth`/`disable_turtle_egg`/`disable_block_growth` in
config) independently suppress natural growth events, unrelated to the resource-block system.

**Player-facing messages**: `Lang` loads `lang.yml` and centralizes cost/reward/deduction
messages; `DataEntry.applyCostsAndRewards` calls into it.

**Command**: `/mirage` (aliases `resources`, `blockregen`, `problockregen`, `pbr`) only supports
`reload`, handled by `CommandExec`, which calls `Mirage.reloadConfigs()`.

## Config format

See the commented example block in `src/main/resources/config.yml` for the full `entries` schema
(materials, break-material, regen-delay, tool.allowed/message/damage, requirements, chance,
rewards, cancel-drop, command, age). Prefixes used for cross-plugin ids: block/tool ids without a
prefix are vanilla `Material` names; `ToolType`/`BlockType` subclasses each define their own
`PREFIX` constant (e.g. Oraxen, ItemsAdder, Sapphire, Divinity) used to disambiguate entries in
config strings like `oraxen:some_id`.
