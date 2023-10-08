set title 'Tests de performances' font 'Verdana,16'

# Libellé des axes
# set xlabel 'Catégories'
set ylabel 'Temps (ms)'

# Changer la police des légendes
set key font 'Verdana,11'

# Définir l'intervalle de l'axe des y
set yrange [0:1100] # Remplacez 0 et 700 par les valeurs de votre choix
# unset xtics

# Définir le style des barres (remplissage solide en bleu)
set style fill solid
set boxwidth 0.2

# Charger les données à partir du fichier donnees.txt
# Utilisez le délimiteur d'espace pour séparer les colonnes
plot 'donnees.txt' using ($0):2 with boxes lc rgb 'blue' title 'Algorithme avec les automates', \
     '' using ($0+0.2):3 with boxes lc rgb 'red' title 'grep -E', \
     '' using ($0+0.4):4 with boxes lc rgb 'green' title 'Algorithme KMP', \
     '' using ($0+0.6):5 with boxes lc rgb 'red' notitle

# Ajouter une description au-dessus de chaque barre
set label "S(a|g|r)+on" at 0, graph 0.95 center font 'Verdana,15'
set label "S(a|g|r)+on" at 0.22, graph 0.05 center font 'Verdana,15'
set label "Sargon" at 0.42, graph 0.07 center font 'Verdana,15'
set label "Sargon" at 0.6, graph 0.05 center font 'Verdana,15'

set label "a|bc*" at 1, graph 0.27 center font 'Verdana,15'
set label "a|bc*" at 1.2, graph 0.08 center font 'Verdana,15'

set label "Bab(y|l|o|n|i|a)+" at 2, graph 0.85 center font 'Verdana,15'
set label "Bab(y|l|o|n|i|a)+" at 2.3, graph 0.05 center font 'Verdana,15'

set label "a+(b*|c*)" at 3, graph 0.25 center font 'Verdana,15'
set label "a+(b*|c*)" at 3.22, graph 0.09 center font 'Verdana,15'

pause -1
