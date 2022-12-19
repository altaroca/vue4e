
# Vue4E - Vue.js Editor for Eclipse

This plugin provides support for Vue Single File Components in eclipse generic text editors.
All of this can be achieved by installing default eclipse plugins and setting some preference options.
This plugin intends to bundle all of this configuration. It does not contain any language support but builds 
on the existing projects:
- [vetur](https://vuejs.github.io/vetur/)
- [volar](https://github.com/johnsoncodehk/volar)
- [LSP4E](https://projects.eclipse.org/projects/technology.lsp4e) 
- [TM4E](https://projects.eclipse.org/projects/technology.tm4e) 

## Prerequisites
- Eclipse includes Language server protocol for Eclipse (lsp4e) since ca. 2018. 
- Eclipse includes TextMate for Eclipse (tm4e) since ca. 2018. 
- eclipse 2022-12 needs JavaSE-17 to run lsp4e
- Plugin org.eclipse.tm4e.language_pack has the most complete language support
- Plugin org.eclipse.wildwebdeveloper can serve as an alternative
- npm is required on the path to acquire vetur 

## Installation
1. Install vue-language-server (OPTIONAL)
 
 $ npm install vls -g
 
2. Import this project into Eclipse and build
3. Test using Run> Run As ... Eclipse Application
 
## How it works
- LSP4E provides verification, content assist, etc.
- TM4E provides syntax highlighting, etc.

## TODOs
- use volar instead of vetur ?
- use grammars and and language configs from installed volar/vetur
- dynamically decide on grammars provided by plugins existing in the platform 
