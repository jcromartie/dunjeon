(realm town)

(room potion-shop
      "The Potion Shop"
      "All sorts of magical potions are for sale here"
      :south :town-square)

(room town-square
      "Town Square"
      "The center of life here in The Town"
      :north :potion-shop
      :south :armory
      :down :sewers)
