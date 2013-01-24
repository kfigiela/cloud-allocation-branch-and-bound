set term pdf
set out 'czas.pdf'

set xlabel "Liczba procesorow"
set ylabel "Czas"

plot '-' using 1:2 notitle with lp
1 630
2 658.75
3 344.58
4 19.73
6 25
e

set out 'speedup.pdf'

set xlabel "Liczba procesorow"
set ylabel "Speedup"

plot '-' using 1:(630/$2) notitle with lp
1 630
2 658.75
3 344.58
4 19.73
6 25
e