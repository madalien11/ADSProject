// ADS I Class Project
// Single-Cycle RISC-V Core
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 05/10/2023 by Tobias Jauch (@tojauch)

package SimpleRV32I_Tester

import chisel3._
import chiseltest._
import SimpleRV32I._
import org.scalatest.flatspec.AnyFlatSpec

class SimpleRISCV32ITest extends AnyFlatSpec with ChiselScalatestTester {

"SimpleRV32I_Tester" should "work" in {
    test(new SimpleRV32I("src/test/programs/BinaryFile")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.clock.setTimeout(0)

      dut.io.result.expect(0.U)     // ADDI x0, x0, 0
      dut.clock.step(1)
      dut.io.result.expect(4.U)     // ADDI x1, x0, 4
      dut.clock.step(1)
      dut.io.result.expect(5.U)     // ADDI x2, x0, 5
      dut.clock.step(1)
      dut.io.result.expect(9.U)     // ADD x3, x1, x2

      dut.clock.step(1)
      dut.io.result.expect(10.U)     // ADDI x4, x2, 5
      dut.clock.step(1)
      dut.io.result.expect(5.U)     // ADD x2, x0, x2
      
      dut.clock.step(1)
      dut.io.result.expect(5.U)     // SUB x5, x4, x2
      
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // AND x5, x0, x5
      dut.clock.step(1)
      dut.io.result.expect(1.U)     // AND x5, x2, x3
      
      dut.clock.step(1)
      dut.io.result.expect(11.U)     // OR x6, x4, x5
      dut.clock.step(1)
      dut.io.result.expect(11.U)     // OR x6, x6, x0
      
      dut.clock.step(1)
      dut.io.result.expect(10.U)     // XOR x6, x6, x5
      dut.clock.step(1)
      dut.io.result.expect(10.U)     // XOR x6, x6, x0
      
      dut.clock.step(1)
      dut.io.result.expect(1.U)     // SLTU x7, x5, x6
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLTU x7, x6, x5
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLTU x7, x0, x7
      dut.clock.step(1)
      dut.io.result.expect(1.U)     // SLTU x7, x0, x6
      
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SLL x7, x0, x5
      dut.clock.step(1)
      dut.io.result.expect(2.U)     // SLL x7, x5, x5
      dut.clock.step(1)
      dut.io.result.expect(8.U)     // SLL x7, x7, x7
      // dut.clock.step(1)
      // dut.io.result.expect(1.U)     // SLL x5, x5, x0 
      // 005012B3 shifting by 0 does not work out

      dut.clock.step(1)
      dut.io.result.expect(4.U)     // SRL x7, x7, x5
      dut.clock.step(1)
      dut.io.result.expect(2.U)     // SRL x7, x7, x5
      dut.clock.step(1)
      dut.io.result.expect(2.U)     // SRL x6, x6, x7
      dut.clock.step(1)
      dut.io.result.expect(0.U)     // SRL x7, x0, x5
        /* 
         * TODO: Add testcases for all R-type instructions in 'BinaryFile' and check the expected results here
         */

      dut.clock.step(1)
      dut.io.result.expect("hFFFFFFFF".U)
           
    }
  }
}

// x0 = 0  0000
// x1 = 4  0100
// x2 = 5  0101
// x3 = 9  1001
// x4 = 10 1010
// x5 = 1  0001
// x6 = 2  0010
// x7 = 0  0000
