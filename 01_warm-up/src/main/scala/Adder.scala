// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chisel3.util._


/** 
  * Half Adder Class 
  * 
  * Your task is to implement a basic half adder as presented in the lecture.
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class HalfAdder extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a half adder as presented in the lecture
     */
     val a = Input(UInt(1.W))
     val b = Input(UInt(1.W))
     val s = Output(UInt(1.W))
     val c = Output(UInt(1.W))
    })

  /* 
   * TODO: Describe output behaviour based on the input values
   */
   io.s := io.a ^ io.b
   io.c := io.a & io.b

}

/** 
  * Full Adder Class 
  * 
  * Your task is to implement a basic full adder. The component's behaviour should 
  * match the characteristics presented in the lecture. In addition, you are only allowed 
  * to use two half adders (use the class that you already implemented) and basic logic 
  * operators (AND, OR, ...).
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FullAdder extends Module{

  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a half adder as presented in the lecture
     */
     val a = Input(UInt(1.W))
     val b = Input(UInt(1.W))
     val ci = Input(UInt(1.W))
     val s = Output(UInt(1.W))
     val co = Output(UInt(1.W))
    })


  /* 
   * TODO: Instanciate the two half adders you want to use based on your HalfAdder class
   */
   val halfadder1 = Module(new HalfAdder())
   val halfadder2 = Module(new HalfAdder())

  /* 
   * TODO: Describe output behaviour based on the input values and the internal signals
   */
   halfadder1.io.a := io.a
   halfadder1.io.b := io.b
   val temps = halfadder1.io.s
   val tempc = halfadder1.io.c

   halfadder2.io.a := temps
   halfadder2.io.b := io.ci
   io.s := halfadder2.io.s
   io.co := halfadder2.io.c ^ tempc

}

/** 
  * 4-bit Adder class 
  * 
  * Your task is to implement a 4-bit ripple-carry-adder. The component's behaviour should 
  * match the characteristics presented in the lecture.  Remember: An n-bit adder can be 
  * build using one half adder and n-1 full adders.
  * The inputs and the result should all be 4-bit wide, the carry-out only needs one bit.
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FourBitAdder extends Module{

  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a 4-bit ripple-carry-adder as presented in the lecture
     */
     val a = Input(UInt(4.W))
     val b = Input(UInt(4.W))
     val s = Output(UInt(4.W))
     val c = Output(UInt(1.W))
    })

  /* 
   * TODO: Instanciate the full adders and one half adderbased on the previously defined classes
   */
   val halfadder = Module(new HalfAdder())
   val fulladder1 = Module(new FullAdder())
   val fulladder2 = Module(new FullAdder())
   val fulladder3 = Module(new FullAdder())


  /* 
   * TODO: Describe output behaviour based on the input values and the internal 
   */
   halfadder.io.a := io.a(0)
   halfadder.io.b := io.b(0)

   fulladder1.io.a := io.a(1)
   fulladder1.io.b := io.b(1)
   fulladder1.io.ci := halfadder.io.c
   
   fulladder2.io.a := io.a(2)
   fulladder2.io.b := io.b(2)
   fulladder2.io.ci := fulladder1.io.co
   
   fulladder3.io.a := io.a(3)
   fulladder3.io.b := io.b(3)
   fulladder3.io.ci := fulladder2.io.co

  //  var result = UInt(4.W)
  //  result = fulladder3.io.s
  //  result = (result << 1) + fulladder2.io.s
  //  result = (result << 1) + fulladder1.io.s
  //  result = (result << 1) + halfadder.io.s
  //  io.s := result
   io.s := ((fulladder3.io.s << 3) | (fulladder2.io.s << 2) | (fulladder1.io.s << 1) | halfadder.io.s)
   io.c := fulladder3.io.co
}
