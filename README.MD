
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
- npm with network connection is required on the path to download vetur 

## Installation
1. Install vue-language-server (OPTIONAL)
 
 $ npm install @volar/vue-language-server -g
 
2. Import this project into Eclipse and build
3. Test using Run> Run As ... Eclipse Application
  - Open a *.vue file to see syntax highlighted.
     NOTE: depending on your existing editor bindings eclipse may use the wrong editor.
     Therefore in Eclipse Projectr Explorer window right-click on the file and choose Open with ... > Generic Text Editor.
  - Right-click in vue file and select "Source ..." to see available code actions.
     NOTE: When doing this for the first time eclipse might freeze for a few seconds up to a few minutes 
     while it downloads and installs vetur.      
4. Install into running platform
   - in Eclipse menu select File > Export ... > Plugin development > Deployable plugins
   - select the plugin and "Install into host" (third option)    

## Troubleshooting
1. Test using Run> Run As ... Eclipse Application
   Enable tracing output with VM argument `-Dvue4e.debug="true"`
2. Observe the console for errors

## How it works
- LSP4E provides verification, content assist, etc.
- TM4E provides syntax highlighting, etc.

## TODOs
- use grammars and and language configs from installed volar/vetur
- dynamically decide on grammars provided by plugins existing in the platform 
