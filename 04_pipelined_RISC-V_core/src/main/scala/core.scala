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
    val req1  = Input(new regFileReadReq)
    val resp1 = Output(new regFileReadResp)
    val req2  = Input(new regFileReadReq)
    val resp2 = Output(new regFileReadResp)
    val writeReq = Input(new regFileWriteReq)
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
  when(io.req1.readAddress =/= 0.U){ io.resp1.readData := regFile(io.req1.readAddress) }
  when(io.req2.readAddress =/= 0.U){ io.resp2.readData := regFile(io.req2.readAddress) }
  
}


// -----------------------------------------
// Fetch Stage
// -----------------------------------------

class IF (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
    val instr = Output(UInt(32.W))
    // val PC = Output(UInt(32.W))
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

  //  io.PC := PC
   io.instr := IMem(PC>>2.U)     
   PC := PC + 4.U
  
}


// -----------------------------------------
// Decode Stage
// -----------------------------------------

class ID extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
        val instr = Input(UInt(32.W))
        val req1 = Output(new regFileReadReq)
        val req2 = Output(new regFileReadReq)
        val resp1 = Input(new regFileReadResp)
        val resp2 = Input(new regFileReadResp)

        val upo = Output(uopc())
        val operandA    = Output(UInt(32.W))
        val operandB    = Output(UInt(32.W))
        val rd    = Output(UInt(5.W))
  })

  /* 
   * TODO: Any internal signals needed?
   */
  val opcode = io.instr(6, 0)
  io.rd := io.instr(11, 7)
  val funct3 = io.instr(14, 12)
  val rs1 = io.instr(19, 15)
  val rs2 = io.instr(24, 20)
  val funct7 = io.instr(31, 25)

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
   io.req1.readAddress := rs1
   io.req2.readAddress := rs2

   io.operandA := io.resp1.readData
   io.operandB := Mux(opcode === "b0010011".U, io.instr(31, 20), io.resp2.readData)
  
}

// -----------------------------------------
// Execute Stage
// -----------------------------------------

class EX extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this pipeline stage need?
    val operandA    = Input(UInt(32.W))
    val operandB    = Input(UInt(32.W))
    val upo = Input(uopc())
    val aluResult = Output(UInt(32.W))
  })

  /* 
    TODO: Perform the ALU operation based on the uopc
    */

  when(io.upo === isADDI) { 
    io.aluResult := io.operandA + io.operandB 
  }.elsewhen(io.upo === isADD) {                           
    io.aluResult := io.operandA + io.operandB
  }.elsewhen(io.upo === isSUB) {
    io.aluResult := io.operandA - io.operandB 
  }.elsewhen(io.upo === isSLL) {
    io.aluResult := io.operandA << io.operandB(4,0)
  }.elsewhen(io.upo === isSRL) {
    io.aluResult := io.operandA >> io.operandB(4,0)
  }.elsewhen(io.upo === isSRA) {
    io.aluResult := ((io.operandA.asSInt) >> io.operandB.asSInt()(4,0)).asUInt
  }.elsewhen(io.upo === isOR) {
    io.aluResult := io.operandA | io.operandB
  }.elsewhen(io.upo === isAND) {
    io.aluResult := io.operandA & io.operandB
  }.elsewhen(io.upo === isXOR) {
    io.aluResult := io.operandA ^ io.operandB
  }.elsewhen(io.upo === isSLT) {
    io.aluResult := Mux(io.operandB.asSInt =/= 0.S, Mux(io.operandA.asSInt < io.operandB.asSInt, 1.S, 0.S), 0.S).asUInt
  }.elsewhen(io.upo === isSLTU) {
    io.aluResult := io.operandA < io.operandB
  }.elsewhen(io.upo === invalid) {
    io.aluResult := io.operandA + io.operandB
  }.otherwise {
    io.aluResult := "h_0000_0000".U
  }
  
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
    val aluResult = Input(UInt(32.W))
    val rd = Input(UInt(5.W))
    val check_res = Output(UInt(32.W))
    val req = Output(new regFileWriteReq)
  })

  /* 
   * TODO: Perform the write back to the register file and set 
   *       the check_res signal for the testbench.
   */
   io.req.writeAddress := io.rd
   io.req.writeData := io.aluResult
   io.check_res := io.aluResult

}


// -----------------------------------------
// IF-Barrier
// -----------------------------------------

class IFBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
    val instrIn = Input(UInt(32.W))
    val instrOut = Output(UInt(32.W))
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
   */
   val instr = RegInit(UInt(32.W), 0.U)
   instr := io.instrIn
   io.instrOut := instr


}


// -----------------------------------------
// ID-Barrier
// -----------------------------------------

class IDBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
    val upoIn = Input(uopc())
    val operandAIn    = Input(UInt(32.W))
    val operandBIn    = Input(UInt(32.W))
    val rdIn    = Input(UInt(5.W))
    
    val upoOut = Output(uopc())
    val operandAOut    = Output(UInt(32.W))
    val operandBOut    = Output(UInt(32.W))
    val rdOut    = Output(UInt(5.W))
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
   */
  val upo = RegInit(uopc(), uopc.invalid)
  val operandA = RegInit(UInt(32.W), 0.U)
  val operandB = RegInit(UInt(32.W), 0.U)
  val rd = RegInit(UInt(5.W), 0.U)

  upo := io.upoIn  
  operandA := io.operandAIn
  operandB := io.operandBIn
  rd := io.rdIn

  io.upoOut := upo
  io.operandAOut := operandA
  io.operandBOut := operandB
  io.rdOut := rd

}



// -----------------------------------------
// EX-Barrier
// -----------------------------------------

class EXBarrier extends Module {
  val io = IO(new Bundle {
    // What inputs and / or outputs does this barrier need?
    val aluResultIn = Input(UInt(32.W))
    val aluResultOut = Output(UInt(32.W))
    val rdIn    = Input(UInt(5.W))
    val rdOut    = Output(UInt(5.W))
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
  */
  val aluResult = RegInit(UInt(32.W), 0.U)
  aluResult := io.aluResultIn
  io.aluResultOut := aluResult

  val rd = RegInit(UInt(5.W), 0.U)
  rd := io.rdIn
  io.rdOut := rd
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
    val check_resIn = Input(UInt(32.W))
    val check_resOut = Output(UInt(32.W))
  })

  /* 
   * TODO: Define registers
   *
   * TODO: Fill registers from the inputs and write regioster values to the outputs
  */
  val check_res = RegInit(UInt(32.W), 0.U)
  check_res := io.check_resIn
  io.check_resOut := check_res

}



class PipelinedRV32Icore (BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val check_res = Output(UInt(32.W))
  })


  /* 
   * TODO: Instantiate Barriers
   */
   val IFb = Module(new IFBarrier)
   val IDb = Module(new IDBarrier)
   val EXb = Module(new EXBarrier)
   val MEMb = Module(new MEMBarrier)
   val WBb = Module(new WBBarrier)


  /* 
   * TODO: Instantiate Pipeline Stages
   */

   val If = Module(new IF(BinaryFile))
   val Id = Module(new ID)
   val Ex = Module(new EX)
   val Mem = Module(new MEM)
   val Wb = Module(new WB)


  /* 
   * TODO: Instantiate Register File
   */
   val registers = Module(new regFile)
   val req1 = Wire(new regFileReadReq)
   val req2 = Wire(new regFileReadReq)
   val reqW = Wire(new regFileWriteReq)
   req1.readAddress := Id.io.req1.readAddress
   req2.readAddress := Id.io.req2.readAddress
   reqW.writeAddress := Wb.io.req.writeAddress
   reqW.writeData := Wb.io.req.writeData

   registers.io.req1 := req1
   registers.io.req2 := req2
   registers.io.writeReq := reqW

  io.check_res := WBb.io.check_resOut // necessary to make the empty design buildable TODO: change this

  /* 
   * TODO: Connect all IOs between the stages, barriers and register file.
   * Do not forget the global output of the core module
   */
   IFb.io.instrIn := If.io.instr
   Id.io.instr := IFb.io.instrOut
   Id.io.resp1 := registers.io.resp1
   Id.io.resp2 := registers.io.resp2
   IDb.io.upoIn := Id.io.upo
   IDb.io.operandAIn := Id.io.operandA
   IDb.io.operandBIn := Id.io.operandB
   IDb.io.rdIn := Id.io.rd
   Ex.io.operandA := IDb.io.operandAOut
   Ex.io.operandB := IDb.io.operandBOut
   Ex.io.upo := IDb.io.upoOut
   EXb.io.aluResultIn := Ex.io.aluResult
   EXb.io.rdIn := IDb.io.rdOut
   Wb.io.aluResult := EXb.io.aluResultOut
   Wb.io.rd := EXb.io.rdOut
   WBb.io.check_resIn := Wb.io.check_res

}

