// ADS I Class Project
// Pipelined RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 01/15/2023 by Tobias Jauch (@tojauch)

/*
The goal of this task is to extend the 5-stage multi-cycle 32-bit RISC-V core from the previous task to a pipelined processor. 
All steps and stages have the same functionality as in the multi-cycle version from task 03, but are supposed to handle different instructions in each stage simultaneously.
This design implements a pipelined RISC-V 32-bit core with five stages: IF (Fetch), ID (Decode), EX (Execute), MEM (Memory), and WB (Writeback).

    Data Types:
        The uopc enumeration data type (enum) defines micro-operation codes representing ALU operations according to the RV32I subset used in the previous tasks.

    Register File (regFile):
        The regFile module represents the register file, which has read and write ports.
        It consists of a 32-entry register file (x0 is hard-wired to zero).
        Reading from and writing to the register file is controlled by the read request (regFileReadReq), read response (regFileReadResp), and write request (regFileWriteReq) interfaces.

    Fetch Stage (IF Module):
        The IF module represents the instruction fetch stage.
        It includes an instruction memory (IMem) of size 4096 words (32-bit each).
        Instructions are loaded from a binary file (provided to the testbench as a parameter) during initialization.
        The program counter (PC) is used as an address to access the instruction memory, and one instruction is fetched in each cycle.

    Decode Stage (ID Module):
        The ID module performs instruction decoding and generates control signals.
        It extracts opcode, operands, and immediate values from the instruction.
        It uses the uopc (micro-operation code) Enum to determine the micro-operation (uop) and sets control signals accordingly.
        The register file requests are generated based on the operands in the instruction.

    Execute Stage (EX Module):
        The EX module performs the arithmetic or logic operation based on the micro-operation code.
        It takes two operands and produces the result (aluResult).

    Memory Stage (MEM Module):
        The MEM module does not perform any memory operations in this basic CPU design.

    Writeback Stage (WB Module):
        The WB module writes the result back to the register file.

    IF, ID, EX, MEM, WB Barriers:
        IFBarrier, IDBarrier, EXBarrier, MEMBarrier, and WBBarrier modules serve as pipeline registers to separate the pipeline stages.
        They hold the intermediate results of each stage until the next clock cycle.

    PipelinedRV32Icore (PipelinedRV32Icore Module):
        The top-level module that connects all the pipeline stages, barriers and the register file.
        It interfaces with the external world through check_res, which is the result produced by the core.

Overall Execution Flow:

    1) Instructions are fetched from the instruction memory in the IF stage.
    2) The fetched instruction is decoded in the ID stage, and the corresponding micro-operation code is determined.
    3) The EX stage executes the operation using the operands.
    4) The MEM stage does not perform any memory operations in this design.
    5) The result is written back to the register file in the WB stage.

Note that this design only represents a simplified RISC-V pipeline. The structure could be equipped with further instructions and extension to support a real RISC-V ISA.
*/

package core_tile

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile


// -----------------------------------------
// Global Definitions and Data Types
// -----------------------------------------

object uopc extends ChiselEnum {

  val isADD   = Value(0x01.U)
  val isSUB   = Value(0x02.U)
  val isXOR   = Value(0x03.U)
  val isOR    = Value(0x04.U)
  val isAND   = Value(0x05.U)
  val isSLL   = Value(0x06.U)
  val isSRL   = Value(0x07.U)
  val isSRA   = Value(0x08.U)
  val isSLT   = Value(0x09.U)
  val isSLTU  = Value(0x0A.U)

  val isADDI  = Value(0x10.U)

  val invalid = Value(0xFF.U)
}

import uopc._


// -----------------------------------------
// Register File
// -----------------------------------------

class regFileReadReq extends Bundle {
    // what signals does a read request need?
    val readAddress = Input(UInt(5.W))
}

class regFileReadResp extends Bundle {
    // what signals does a read response need?
    val readData = Output(UInt(32.W))
}

class regFileWriteReq extends Bundle {
    // what signals does a write request need?
    val writeAddress = Input(UInt(5.W))
    val writeData = Input(UInt(32.W))
}

class regFile extends Module {
  val io = IO(new Bundle {
    val req1  = new regFileReadReq
    val resp1 = new regFileReadResp
    val req2  = new regFileReadReq
    val resp2 = new regFileReadResp
    val writeReq = new regFileWriteReq
    // how many read and write ports do you need to handle all requests
    // from the ipeline to the register file simultaneously?
})
  
  /* 
    TODO: Initialize the register file as described in the task 
          and handle the read and write requests
   */
  val regFile = Mem(32, UInt(32.W))
  regFile.write(0.U, 0.U)

  when(io.writeReq.writeAddress =/= 0.U){ regFile(io.writeReq.writeAddress) := io.writeReq.writeData }

  io.resp1.readData := 0.U
  io.resp2.readData := 0.U
  when(io.req1.readAddress =/= 0.U){ io.resp1.readData := registerFile(io.req1.readAddress) }
  when(io.req2.readAddress =/= 0.U){ io.resp2.readData := registerFile(io.req2.readAddress) }
  
}


// -----------------------------------------
// Fetch Stage
// -----------------------------------------

class IF (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
    val instr = Output(UInt(32.W))
    val PC = Output(UInt(32.W))
  })

  /* 
    TODO: Initialize the IMEM as described in the task 
          and handle the instruction fetch.

    TODO: Update the program counter (no jumps or branches, 
          next PC always reads next address from IMEM)
   */
   val IMem = Mem(4096, UInt(32.W))
   loadMemoryFromFile(IMem, BinaryFile)

   val PC = RegInit(UInt(32.W), 0.U)

   io.PC := PC
   io.instr := IMem(PC>>2.U)     
   PC := PC + 4.U
  
}


// -----------------------------------------
// Decode Stage
// -----------------------------------------

// The register file requests are generated based on the operands in the instruction.
class ID extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
        val PCIn = Input(UInt(32.W))
        val instr = Input(UInt(32.W))
        // val writeData = Input(UInt(32.W))
        // val writeAddress = Input(UInt(32.W))
        
        val PCOut = Output(UInt(32.W))
        val upo = Output(new uopc)
        // val op1SelectOut      = Output(UInt(1.W))
        // val op2SelectOut      = Output(UInt(1.W))
        // val immTypeOut        = Output(UInt(3.W))
        // val ALUopOut          = Output(UInt(4.W))

        val operandA    = Output(UInt(32.W))
        val operandB    = Output(UInt(32.W))
  })

  /* 
   * TODO: Any internal signals needed?
   */
  val opcode = instr(6, 0)
  val rd := instr(11, 7)
  val funct3 = instr(14, 12)
  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val funct7 = instr(31, 25)

  /* 
    Determine the uop based on the disassembled instruction
    */
  when( opcode === "b0110011".U ){
    when( funct3 === "b000".U && funct7 === "b0000000".U ){
      io.upo := isADD
    }.elsewhen( funct3 === "b000".U && funct7 === "b0100000".U ){
      io.upo := isSUB
    }.elsewhen( funct3 === "b111".U && funct7 === "b0000000".U ){
      io.upo := isAND
    }.elsewhen( funct3 === "b110".U && funct7 === "b0000000".U ){
      io.upo := isOR
    }.elsewhen( funct3 === "b100".U && funct7 === "b0000000".U ){
      io.upo := isXOR
    }.elsewhen( funct3 === "b010".U && funct7 === "b0000000".U ){
      io.upo := isSLT
    }.elsewhen( funct3 === "b011".U && funct7 === "b0000000".U ){
      io.upo := isSLTU
    }.elsewhen( funct3 === "b001".U && funct7 === "b0000000".U ){
      io.upo := isSLL
    }.elsewhen( funct3 === "b101".U && funct7 === "b0000000".U ){
      io.upo := isSRL
    }.elsewhen( funct3 === "b101".U && funct7 === "b0100000".U ){
      io.upo := isSRA
    }.otherwise{
      io.upo := invalid
    } 
  }.elsewhen( opcode === "b0010011".U ){
    when( funct3 === "b000".U ){
      io.upo := isADDI
    }.otherwise{
      io.upo := invalid
    } 
  }.otherwise{
    io.upo := invalid
  }

  /* 
   * TODO: Read the operands from teh register file
   */
   io.PCOut := io.PCIn
   operandA := regFile.read(rs1)
   operandB := Mux(opcode === "b0010011".U, instr(31, 20), regFile.read(rs2))
  
}

// -----------------------------------------
// Execute Stage
// -----------------------------------------

class EX extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
  })

  /* 
    TODO: Perform the ALU operation based on the uopc

    when( uopc === isXYZ ){
      result := operandA + operandB
    }.elsewhen( uopc === isABC ){
      result := operandA - operandB
    }.otherwise{
      maybe also declare a case to catch invalid instructions
    }
  */
}

// -----------------------------------------
// Memory Stage
// -----------------------------------------

class MEM extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
  })

  // No memory operations implemented in this basic CPU

}


// -----------------------------------------
// Writeback Stage
// -----------------------------------------

class WB extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
  })

  /* 
   * TODO: Perform the write back to the register file and set 
   *       the check_res signal for the testbench.
   */

}


// -----------------------------------------
// IF-Barrier
// -----------------------------------------

class IFBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
   */

}


// -----------------------------------------
// ID-Barrier
// -----------------------------------------

class IDBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
   */

}


// -----------------------------------------
// EX-Barrier
// -----------------------------------------

class EXBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
  */

}


// -----------------------------------------
// MEM-Barrier
// -----------------------------------------

class MEMBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
  */

}


// -----------------------------------------
// WB-Barrier
// -----------------------------------------

class WBBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
  */

}



class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W))
  })


  /* 
   * TODO: Instantiate Barriers
   */


  /* 
   * TODO: Instantiate Pipeline Stages
   */


  /* 
   * TODO: Instantiate Register File
   */

  io.check_res := 0.U // necessary to make the empty design buildable TODO: change this

  /* 
   * TODO: Connect all IOs between the stages, barriers and register file.
   * Do not forget the global output of the core module
   */

}

