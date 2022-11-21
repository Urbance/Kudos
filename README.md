# Kudos
<p align="center">
  <img src="https://github.com/Urbance/Kudos/blob/main/logo.hereo-of-the-village.png" title="Logo-Hero-of-the-Village">
</p>

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Requirements](#requirements)
4. [Installation](#installation)
5. [Commands and Permissions](#commands-and-permissions)
6. [FAQs](#faqs)

## Introduction
Kudos is an open source plugin for Minecraft 1.19 Spigot Server. The plugin allows to give someone a kudo (recognition/praise).<br>
It includes a GUI, which can display various informations and statistics.

## Features
* MySQL
* Editable messages
* Configurable
* Item reward

## Requirements
* MySQL Database
* Requires Java 17 or later

## Installation
* Download the plugin .jar 
* Drag the .jar into your plugin folder
* Start your server

## Commands and Permissions
| Command | Permission | Description |
| ------------- | ------------- | ------------- |
| <b>👥 Player  | kudos.* | |
| kudo [player]  | kudos.award  | Award a player a Kudo | 
| kudos  | kudos.gui  | Open GUI  |
| kudos [player]  | kudos.show  | Displays the Kudos from a player |
| ⠀| | |
| <b>⚙️ Administrator  | kudmin | |
| kudmin | | Show plugin version |
| kudmin help  | | Show help page |
| kudmin add [kudos/assigned_kudos] [player] [amount] | | Add (assigned) Kudos for a player |
| kudmin remove [kudos/assigned_kudos] [player] [amount] | | Remove (assigned) Kudos from a player|
| kudmin set [player] [kudos/assigned_kudos] [value] | | Set (assigned) Kudos for a player |
| kudmin clear [kudos/assigned_kudos] [player] | | Clear all (assigned) Kudos from a player|
| kudmin reload | | Reload plugin files |

## FAQs
***
Q: I have Questions, Suggestions or Issues. How can I contact you?

A: I am glad that you want to contact me. Join my [Discord Server](https://discord.gg/hDqPms3MbH) and open a ticket or write me a PM. 
