set title 'Tests de performances en moyenne' font 'Verdana,16'

# Libellé des axes
# set xlabel 'Catégories'
set ylabel 'Temps (ms)'

# Changer la police des légendes
set key font 'Verdana,11'

# Définir l'intervalle de l'axe des y
set yrange [0:700] # Remplacez 0 et 700 par les valeurs de votre choix
# unset xtics

# Définir le style des barres (remplissage solide en bleu)
set style fill solid
set boxwidth 0.5

# Charger les données à partir du fichier donnees.txt
# Utilisez le délimiteur d'espace pour séparer les colonnes
plot 'donnees.txt' using ($0):2 with boxes lc rgb 'blue' title 'Algorithme automates', \
     '' using ($0+0.5):3 with boxes lc rgb 'red' title 'Algorithme KMP'

# Ajouter une description au-dessus de chaque barre
set label "S(a|g|r)+on" at 0, graph 0.92 center font 'Verdana,10'
set label "Sargon" at 0.5, graph 0.05 center font 'Verdana,10'

pause -1
