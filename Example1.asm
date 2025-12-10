# Example 1: Simple Attack and Heal
# $t0 = Player HP
# $t2 = Attack damage
# $t3 = Heal amount

seti $t0, 50       # Player HP = 50
seti $t2, 20       # Attack damage = 20

move $t0, $t2      # Apply damage: HP -= damage
inspect $t0        # Print HP after attack

heal $t0, 10       # HP += heal
inspect $t0        # Print HP after healing
