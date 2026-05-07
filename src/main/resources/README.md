APLICATIE DE GESTIUNE A UNEI COFETARII

Baza de date POSTGRESQL
Relatii 1-n (cofetar - torturi)
m-n (clienti - torturi)

Arhitectura stratificata
domain - are clasele Cofetar, Client, Tort
repository - o clasa in care setez url-ul, parola si userul
service - face legatura intre repository si controller
controller - face legatura intre date si interfata

Interfata
2 tabele: unul parinte cu cofetarii si unul copil cu torturile pentru fiecare cofetar