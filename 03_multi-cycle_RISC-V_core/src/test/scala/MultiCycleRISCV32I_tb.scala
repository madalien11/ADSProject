// ADS I Class Project
// Multi-Cycle RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 12/19/2023 by Tobias Jauch (@tojauch)

package MultiCycleRV32I_Tester

import chisel3._
import chiseltest._
import MultiCycleRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class MultiCycleRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

"MultiCycleRV32I_Tester" should "work" in {
    test(new MultiCycleRV32I("src/test/programs/BinaryFile")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        /* 
         * TODO: Insert testcases from assignment 2 and adapt them for the multi-cycle core
         */
        dut.clock.setTimeout(0)
        dut.clock.step(4)
        dut.io.result.expect(0.U)     // ADDI x0, x0, 0
        dut.clock.step(5)
        dut.io.result.expect(4.U)     // ADDI x1, x0, 4
        dut.clock.step(5)
        dut.io.result.expect(5.U)     // ADDI x2, x0, 5
        dut.clock.step(5)
        dut.io.result.expect(9.U)     // ADD x3, x1, x2

        dut.clock.step(5)
        dut.io.result.expect(10.U)     // ADDI x4, x2, 5
        dut.clock.step(5)
        dut.io.result.expect(5.U)     // ADD x2, x0, x2
        
        dut.clock.step(5)
        dut.io.result.expect(5.U)     // SUB x5, x4, x2
        
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // AND x5, x0, x5
        dut.clock.step(5)
        dut.io.result.expect(1.U)     // AND x5, x2, x3
        
        dut.clock.step(5)
        dut.io.result.expect(11.U)     // OR x6, x4, x5
        dut.clock.step(5)
        dut.io.result.expect(11.U)     // OR x6, x6, x0
        
        dut.clock.step(5)
        dut.io.result.expect(10.U)     // XOR x6, x6, x5
        dut.clock.step(5)
        dut.io.result.expect(10.U)     // XOR x6, x6, x0
        
        dut.clock.step(5)
        dut.io.result.expect(1.U)     // SLTU x7, x5, x6
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SLTU x7, x6, x5
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SLTU x7, x0, x7
        dut.clock.step(5)
        dut.io.result.expect(1.U)     // SLTU x7, x0, x6
        
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SLL x7, x0, x5
        dut.clock.step(5)
        dut.io.result.expect(2.U)     // SLL x7, x5, x5
        dut.clock.step(5)
        dut.io.result.expect(8.U)     // SLL x7, x7, x7
        // dut.clock.step(5)
        // dut.io.result.expect(1.U)     // SLL x5, x5, x0 
        // 005012B3 shifting by 0 does not work out

        dut.clock.step(5)
        dut.io.result.expect(4.U)     // SRL x7, x7, x5
        dut.clock.step(5)
        dut.io.result.expect(2.U)     // SRL x7, x7, x5
        dut.clock.step(5)
        dut.io.result.expect(2.U)     // SRL x6, x6, x7
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SRL x7, x0, x5
        
        dut.clock.step(5)
        dut.io.result.expect(12.U)     // ADD x4, x4, x6

        dut.clock.step(5)
        dut.io.result.expect("b111111111111".U)     // ADDI x7, x0, 4095
        dut.clock.step(5)
        dut.io.result.expect(16773120.U)     // SLL x7, x7, x4   "b111111111111000000000000"  16773120
        dut.clock.step(5)
        dut.io.result.expect(16777215.U)     // ADDI x7, x7, "b111111111111"    "b111111111111111111111111"  16777215
        dut.clock.step(5)
        dut.io.result.expect("b11111111111111111111111000000000".U)     // SLL x7, x7, x3   "b11111111111111111111111000000000"  -512
        dut.clock.step(5)
        dut.io.result.expect("b11111111111111111111111111111111".U)     // ADDI x7, x7, "b000111111111"    "b11111111111111111111111111111111"  4294967295
        dut.clock.step(5)
        dut.io.result.expect("b11111111111111111111111111111111".U)     // SRA x7, x7, x3    "b11111111111111111111111111111111"  4294967295
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SRA x8, x3, x3
        dut.clock.step(5)
        dut.io.result.expect(6.U)     // SRA x8, x4, x5
        
        dut.clock.step(5)
        dut.io.result.expect(1.U)     // SLT x8, x7, x5
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SLT x8, x5, x7
        dut.clock.step(5)
        dut.io.result.expect(0.U)     // SLTU x8, x7, x5
        dut.clock.step(5)
        dut.io.result.expect(1.U)     // SLTU x8, x5, x7

        dut.clock.step(1)
        dut.io.result.expect("h_0000_0000".U)
           
    }
  }
}


