# Example 2: Type Effectiveness + Critical Hit Test
# $t1 = Defender HP
# $t2 = Attacker base power
# $t6 = Attacker type
# $t7 = Defender type
# $s2 = Temporary power

seti $t1, 100		# Defender HP = 100
settypep1 $t6, 1	# Attacker type = Fire
settypep2 $t7, 4	# Defender type = Grass

seti $t2, 10		# Base attack power = 10

temppower $s2, $t2	# Load temp power from base stat
typeeff $s2, $t7	# Apply type effectiveness (Fire vs Grass = super effective)
crit $s2 		# Critical hit (double damage)

move $t1, $s2		# Apply final damage
inspect $t1		# Print defender HP after attack
