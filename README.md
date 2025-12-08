> GitHub is only used for Issuetracking. The main repository is hosted at GitLab: https://gitlab.eps-dev.de/Lilly/kittypaper

<div align="center">

## KittyPaper

[![Join us on Discord](https://discord.com/api/guilds/1350838001403564172/widget.png?style=shield)](https://discord.gg/USab3KXAza)

KittyPaper is a drop-in replacement for [Paper](https://github.com/PaperMC/Paper) servers designed with privacy in mind. It aims to provide the same performance and features as Paper while enhancing user privacy by anonymizing player data and blocking unwanted requests.
</div>

## Features

The current differences between KittyPaper and Paper are:

- `hide-online-players` in `server.properties` is set to `true` by default to enhance player privacy.
- Players are always anonymized in the player sample sent by the server list ping response. 
- Request blocking using the [KittyBlock](https://github.com/LillySchramm/KittyScanBlocklist) blocklist by default.
- A dashboard to view the requests blocked by your server: [Demo](https://kittypaper.com/dash/2c1a4412-cd34-4515-bee5-a0bb28c8d28c)

## Configuration

See the Documentation for configuration options: [https://kittypaper.com/docs](https://kittypaper.com/docs)


## Contact
Join us on Discord:

[![Join us on Discord](https://discord.com/api/guilds/1350838001403564172/widget.png?style=banner2)](https://discord.gg/USab3KXAza)

## Issues

If you encounter any issues or have feature requests, please report them on [GitHub](https://github.com/LillySchramm/KittyPaper/issues/new).

## Credits
Developed and provided by Lilly Schramm (https://schramm.software)

Logo design and design consultation by Lona Linder (https://lona.moe)

## License
All patches are licensed under the [MIT license](https://gitlab.eps-dev.de/Lilly/kittypaper/-/blob/main/LICENSE), unless otherwise noted in the patch headers.

See [PaperMC/Paper](https://github.com/PaperMC/Paper), and [PaperMC/Paperweight](https://github.com/PaperMC/paperweight) for the license of material used by this project.