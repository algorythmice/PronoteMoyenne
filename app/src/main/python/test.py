import datetime

import pronotepy
from pronotepy.ent import monlycee

try:
    client = pronotepy.Client("https://0782549x.index-education.net/pronote/eleve.html",
                              username="alexis.josseaume",
                              password="@Pronote2025",
                              ent=monlycee)
    if client.logged_in:
        print("connexion réussie")
    date_from = datetime.date(2025, 12, 19)
    for i in client.get_recipients():
        print(i.name)
    infos = client.info.class_name
    print(infos)


except Exception as e:
    print(e)