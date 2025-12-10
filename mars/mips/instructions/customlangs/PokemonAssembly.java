package mars.mips.instructions.customlangs;
import mars.simulator.*;
import mars.mips.hardware.*;
import mars.mips.instructions.syscalls.*;
import mars.*;
import mars.util.*;
import java.util.*;
import java.io.*;
import mars.mips.instructions.*;
import java.util.Random;

public class PokemonAssembly extends CustomAssembly{
    //Simple PRNG seed for RNG instruction (needs to be static to persist across calls)
    private static int lcgSeed = 0x1234567;  //arbitrary non-zero seed

    @Override
    public String getName(){

        return "PokemonAssembly";
    }

    @Override
    public String getDescription(){

        return "Assembly language replicating simple Pokemon mechanics in MARS.";
    }

    @Override
    protected void populate() {
        // Basic Instructions:
        // 1) MOVE: move $t1, $t2
        // R-format: opcode = 000000 funct = 000001
        instructionList.add(
                new BasicInstruction("move $t1, $t2",
                        "Apply damage: R[$t1] = R[$t1] - R[$t2]",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000001",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int destReg = operands[0];
                                int srcReg = operands[1];
                                int destVal = RegisterFile.getValue(destReg);
                                int srcVal = RegisterFile.getValue(srcReg);
                                RegisterFile.updateRegister(destReg, destVal - srcVal);
                            }
                        }));
        // 2) HEAL: heal $t0, imm
        // I-format: opcode = 001000
        instructionList.add(
                new BasicInstruction("heal $t0, -100",
                        "Heal immediate: R[$t0] += imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001000 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int imm = operands[1] << 16 >> 16; // sign-extend 16-bit immediate here
                                int val = RegisterFile.getValue(reg);
                                int healed = val + imm;
                                RegisterFile.updateRegister(reg, healed);
                            }
                        }));
        // 3) SETTYPE: settype $t6, imm
        // I-format: opcode = 001001
        instructionList.add(
                new BasicInstruction("settypep1 $t6, -100",
                        "Set type code: R[$t6] = imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001001 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int imm = statement.getOperands()[1] << 16 >> 16; // sign-extend 16-bit immediate
                                RegisterFile.updateRegister(14, imm);  //$t6 is register 14
                            }
                        }));
        // Had to make a second settype for defender
        instructionList.add(
                new BasicInstruction("settypep2 $t7, -100",
                        "Set type code: R[$t7] = imm",
                        BasicInstructionFormat.I_FORMAT,
                        "101001 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int imm = statement.getOperands()[1] << 16 >> 16; // sign-extend 16-bit immediate
                                RegisterFile.updateRegister(15, imm);  //$t7 is register 14
                            }
                        }));
        // 4) BUFF: buff $s2, imm
        // I-format: opcode = 001010
        instructionList.add(
                new BasicInstruction("buff $s2, -100",
                        "Increase temporary power: R[$s2] += imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001010 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int imm = operands[1] << 16 >> 16; // sign-extend 16-bit immediate here
                                int val = RegisterFile.getValue(reg);
                                RegisterFile.updateRegister(reg, val + imm);
                            }
                        }));
        // 5) DEBUFF: debuff $s2, imm
        // I-format: opcode = 001011
        instructionList.add(
                new BasicInstruction("debuff $s2, -100",
                        "Decrease temporary power: R[$s2] -= imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001011 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int imm = operands[1] << 16 >> 16; // sign-extend 16-bit immediate here
                                int val = RegisterFile.getValue(reg);
                                RegisterFile.updateRegister(reg, val - imm);
                            }
                        }));
        // 6) INSPECT: inspect $t0 ($t0 as an example)
        // R-format: opcode = 000000 funct = 000010
        instructionList.add(
                new BasicInstruction("inspect $t0",
                        "Print register (stat) value to MARS console",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000010",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int val = RegisterFile.getValue(reg);
                                SystemIO.printString("Let's look at the stat here (" + regName(reg) + "): " + val
                                        + "\n");
                            }
                        }));
        // 7) ZEROHP: zerohp $t0 (P1 HP = 0)
        // R-format: opcode = 000000 funct = 000011
        instructionList.add(
                new BasicInstruction("zerohp $t0",
                        "Set HP to zero (fainted): R[$t0] = 0",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000011",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                RegisterFile.updateRegister(reg, 0);
                            }
                        }));
        // 8) SETSTATUS: setstatus $s0, imm
        // I-format: opcode = 001100
        instructionList.add(
                new BasicInstruction("setstatus $s0, -100",
                        "Apply status bits: R[$s0] |= imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001100 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int imm = operands[1] << 16 >> 16; // sign-extend 16-bit immediate here
                                int val = RegisterFile.getValue(reg);
                                RegisterFile.updateRegister(reg, val | imm);
                            }
                        }));
        // 9) CLEARSTATUS: resetstatus $s0, imm
        // I-format: opcode = 001101
        instructionList.add(
                new BasicInstruction("resetstatus $s0, -100",
                        "Clear status bits: R[$s0] &= ~imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001101 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                int imm = operands[1] << 16 >> 16; // sign-extend 16-bit immediate here
                                int val = RegisterFile.getValue(reg);
                                RegisterFile.updateRegister(reg, val & ~imm);
                            }
                        }));
        // 10) TEMPPOWER: temppower $s2, $t2 (temp power = P1 Attack)
        // R-format: opcode = 000000 funct = 000100
        instructionList.add(
                new BasicInstruction("temppower $s2, $t2",
                        "Load Pokemon stat into temporary power register: R[$s2] = R[$t2]",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000100",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int tempReg = operands[0];
                                int srcReg = operands[1];
                                int val = RegisterFile.getValue(srcReg);
                                RegisterFile.updateRegister(tempReg, val);
                            }
                        }));
        // Unique instructions:
        // 11) TYPEEFF: typeeff $s2, $t7 (temp power vs defender P2 type)
        // R-format: opcode = 000000 funct = 000101
        instructionList.add(
                new BasicInstruction("typeeff $s2, $t7",
                        "Apply a simple type effectiveness multiplier based on attacker($t6) vs defender ($t7).",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000101",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int tempReg = operands[0];
                                int defTypeReg = operands[1];
                                int power = RegisterFile.getValue(tempReg);
                                // Attacker type is assumed in $t6 (register 14)
                                int attackerType = RegisterFile.getValue(14);
                                int defenderType = RegisterFile.getValue(defTypeReg);
                                // For now I'm only implementing 4 types
                                // Simple rules:
                                // Fire (1) > Grass (4)
                                // Water (2) > Fire (1)
                                // Grass (4) > Water (2)
                                // Electric (3) > Water (2)
                                // Otherwise neutral
                                int newPower = power;
                                if ((attackerType == 1 && defenderType == 4) ||
                                        (attackerType == 2 && defenderType == 1) ||
                                        (attackerType == 4 && defenderType == 2) ||
                                        (attackerType == 3 && defenderType == 2)) {
                                    newPower = power * 2; // super effective
                                } else if ((attackerType == 1 && defenderType == 2) ||
                                        (attackerType == 2 && defenderType == 4) ||
                                        (attackerType == 4 && defenderType == 1)) {
                                    newPower = power / 2; // not very effective...
                                }
                                RegisterFile.updateRegister(tempReg, newPower);
                            }
                        }));
        // 12) CRIT: crit $s2
        // R-format: opcode = 000000 funct = 000110
        instructionList.add(
                new BasicInstruction("crit $s2",
                        "Critical hit: doubles temporary power (R[$s2] *= 2).",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000110",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int tempReg = operands[0];
                                int val = RegisterFile.getValue(tempReg);
                                RegisterFile.updateRegister(tempReg, val * 2);
                            }
                        }));
        // 13) BALL: ball $s3, $s1
        // R-format: opcode = 000000 funct = 000111
        instructionList.add(
                new BasicInstruction("ball $s3, $s1",
                        "Use an item to modify catch chance. Items: 1 = Poke Ball (+1), 2 = Great Ball (+3), 3 = Ultra" +
                                "Ball (+5).",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss ddddd 00000 000111",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int catchReg = operands[0];
                                int itemReg = operands[1];
                                int catchVal = RegisterFile.getValue(catchReg);
                                int itemCode = RegisterFile.getValue(itemReg);
                                int bonus = 0;
                                if (itemCode == 1) {
                                    bonus = 1;
                                } else if (itemCode == 2) {
                                    bonus = 3;
                                } else if (itemCode == 3) {
                                    bonus = 5;
                                }
                                RegisterFile.updateRegister(catchReg, catchVal + bonus);
                            }
                        }));
        // 14) CATCH: catch $s3, label
        // Branch-format: opcode = 000100 (like beq style) ; operands[1] will be the label/offset
        instructionList.add(
                new BasicInstruction("catch $s3, label",
                        "Attempt a capture: if R[$s3] > defender HP ($t1), branch to label (success).",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000100 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int catchReg = operands[0];
                                int labelAddressOrOffset = operands[1]; //MARS provides the resolved label/offset here
                                int catchVal = RegisterFile.getValue(catchReg);
                                // Defender HP is in $t1 (register 9)
                                int defenderHP = RegisterFile.getValue(9); // $t1
                                if (catchVal > defenderHP) {
                                    //Branch to label: processBranch expects the target
                                    Globals.instructionSet.processBranch(labelAddressOrOffset);
                                }
                            }
                        }));
        // 15) RNG: rng $s3
        // I-format: opcode = 001110 (using immediate field for optional seed or 0)
        instructionList.add(
                new BasicInstruction("rng $s3, -100",
                        "Generate a deterministic random value (0-32767) in R[$s3].",
                        BasicInstructionFormat.I_FORMAT,
                        "001110 fffff 00000 ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int reg = operands[0];
                                // Linear congruential generator (32-bit), persistent across calls via lcgSeed
                                lcgSeed = (lcgSeed * 1103515245 + 12345) & 0x7FFFFFFF;
                                int value = (lcgSeed >>> 1) & 0x7FFF; //0-32767
                                RegisterFile.updateRegister(reg, value);
                            }
                        }));
        // 16) SETI: seti $t0, imm (not a unique function but a necessary li function)
        // I-format: opcode = 001111
        instructionList.add(
                new BasicInstruction("seti $t0, -100",
                        "Load an immediate value into a register.",
                        BasicInstructionFormat.I_FORMAT,
                        "001111 fffff ttttt ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int imm = operands[1] << 16 >> 16;
                                RegisterFile.updateRegister(operands[0], imm);
                            }
                        }));
    }
    private String regName(int regNum) {
        switch (regNum) {
            case 8: return "$t0";
            case 9: return "$t1";
            case 10: return "$t2";
            case 11: return "$t3";
            case 12: return "$t4";
            case 13: return "$t5";
            case 14: return "$t6";
            case 15: return "$t7";
            case 16: return "$s0";
            case 17: return "$s1";
            case 18: return "$s2";
            case 19: return "$s3";
            default: return "$r" + regNum;
        }
    }
}