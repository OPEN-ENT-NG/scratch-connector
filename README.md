# À propos de l'application Connecteur Scratch

* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Régions Normandie, Nouvelle Aquitaine
* Financeur(s) : Régions Normandie, Nouvelle Aquitaine
* Développeur(s) : CGI
* Description : Module permettant la connexion à Scratch

# Présentation du module
Scratch est un outil de premiers pas vers la programmation. Il permet aux élèves d'utiliser sur une interface graphique ludique et éducative, des blocs d'action leur permettant de créer des histoires et des animations.

## Configuration

<pre>
{
  "config": {
    ...
    "scratch-url": "${scratchURL}",
    "scratch-cron": "${scratchCron}",
    "scratch-session-duration": "${scratchSessionDuration}"
  }
}
</pre>

Dans votre springboard, vous devez inclure des variables d'environnement :

<pre>
scratchURL = ${String}
scratchCron = ${String}
scratchSessionDuration = Integer
</pre>

