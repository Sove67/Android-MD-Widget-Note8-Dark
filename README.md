# Markdown Widget
A minimalistic widget with the content of a markdown file to your home screen, forked from [here](https://github.com/Tiim/Android-Markdown-Widget).
This app is intended for use with [Obsidian](https://obsidian.md), but should display any .md file
saved using CommonMark formatting. It may also partially function with other formats.

## Features
* Open any markdown or text file on your phone and display it as an home screen widget.
* The widget is updated in a regular, 30 minute interval.
* Transparent Note Background
* White Text designed for dark wallpapers
* Scrolling
* Note Title
* Prefix Formatting
  * Headers 1 - 3
  * Bullet Lists
  * Checkbox (Non Interactive)

Currently the widget is non-interactive. Tapping on links or checkboxes does not do anything.

## Planned Features
* Checkbox Interaction
* Checkbox Colouration
* Inline formatting
  * Italics
  * Bolding
  * strikethrough
  * Links
* View File In Obsidian when title is tapped
* Reactive Widget Updates

## Known Bugs
* Prefix formats (dashes and hashtags at the moment) will be converted even if they are in the middle of a line when viewing the preview in the app. It doesn't seem to happen in the widget.
* Checkboxes don't display as purple in the widget, only the app
* While changing the selected document via the configuration menu does update the widget's data, it does not refresh it's display. Currently, the user must wait for the next cycle of the 30 minute interval update schedule. Alternatively, they can delete the widget and add it once more to force an update.

## License
[GNU General Public License v3.0](https://github.com/Sove67/Android-MD-Widget-Note8-Dark/blob/main/LICENSE.md)
