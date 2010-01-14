(realm town)

(room town-square
      "Town Square"
      "The center of life here in The Town"
      :north potion-shop
      :down sewers)

(room potion-shop
      "The Potion Shop"
      "All sorts of magical potions are for sale here"
      :south town-square)

(room sewers
      "Sewer entrance"
      "Gross..."
      :up town-square
      :south sewers-south
      :north sewer-hub)

(room sewer-hub
      "Sewer Hub"
      "A black pit seems to descend forever in the center of the central
point of the sewer system here. Branches extend in all directions."
      :north sewers-north
      :south sewers
      :east sewers-east
      :west sewers-west
      :down pit)

(room sewers-south
      "Sewers, South"
      "(Mostly) water pours out of a large grating at the end of the pipe here.
It looks like a person could squeeze through"
      :north sewers
      :south forest/sewer-output)
