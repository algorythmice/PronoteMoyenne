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
    for i in client.homework(date_from):
        print(i.date)
        print(i.subject.name)
        print(i.description)
        print("\n")


except Exception as e:
    print(e)