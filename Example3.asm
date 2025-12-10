# Example 3: Revive a fainted Pokemon
# $t0 = Pokemon HP
# $s2 = Revive amount (temporary power)

seti $t0, 40		# Initial HP = 40
inspect $t0		# Display initial HP

zerohp $t0		# Pokemon faints (HP = 0)
inspect $t0		# Confirm fainted state

seti $s2, 20 		# Revive amount = 20 HP
heal $t0, 20		# Revive by healing HP += 20
inspect $t0		# Show revived HP