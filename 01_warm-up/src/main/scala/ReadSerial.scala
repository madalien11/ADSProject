// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
     val rxd = Input(UInt(1.W))
     val reset_n = Input(UInt(1.W))
     val cnt_s = Input(UInt(3.W))
     val cnt_en = Output(UInt(1.W))
     val valid = Output(UInt(1.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
   var enabler = RegInit(0.U(1.W))
  //  var enabler = 0

  // state machine
  /* 
   * TODO: Describe functionality if the controller as a state machine
   */
   when(io.reset_n === 0.U) {
    // when(io.cnt_s === "b111".U) {
    //   io.valid := 1.U
    //   io.cnt_en := 0.U
    //   enabler = 0
    // } .elsewhen(io.rxd === 0.U) {
    //   io.valid := 0.U
    //   io.cnt_en := 1.U
    //   printf("am i even here")
    //   enabler = 1
    // } .elsewhen(enabler.asUInt(1.W) === 1.U) {
    //   io.valid := 0.U
    //   io.cnt_en := 1.U
    //   enabler = 1
    // } .otherwise {
    //   io.valid := 0.U
    //   io.cnt_en := 0.U
    //   enabler = 0
    // }
    io.cnt_en := Mux((io.rxd === 0.U || enabler === 1.U) && io.cnt_s =/= "b111".U, 1.U, 0.U)
    when(io.rxd === 0.U) {
      enabler := 1.U
    }. elsewhen(io.cnt_s === "b111".U) {
      enabler := 0.U
    }
    io.valid := Mux(io.cnt_s === "b111".U, 1.U, 0.U)
   } otherwise {
    io.valid := 0.U
    io.cnt_en := 0.U
    enabler := 0.U
   }
}


/** counter class */
class Counter extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
     val reset_n = Input(UInt(1.W))
     val cnt_en = Input(UInt(1.W))
     val cnt_s = Output(UInt(3.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
  //  var value = 0.U
   val value = RegInit("b000".U(3.W))
   val enabler1 = RegInit(0.U)
   val enabler2 = RegInit(0.U)

  // state machine
  /* 
   * TODO: Describe functionality if the counter as a state machine
   */
   when(io.reset_n === 0.U) {
    printf("where my fckn enable at = %b \n", io.cnt_en)
    when(io.cnt_en === 1.U && enabler2 === 1.U) {
      value := (value + 1.U) % 8.U
    }
    enabler2 := enabler1
    enabler1 := io.cnt_en
   } .otherwise {
    value := "b000".U
   }
  //  printf("value0 = %b \n", value)
   io.cnt_s := value
   
  //  when(io.reset_n === 0.U) {
  //   when(io.cnt_en === 1.U && enabler === false.B) {
  //     enabler := true.B
  //   } .elsewhen(enabler === true.B) {
  //     value := (value + 1.U)
  //   }
  //  } otherwise {
  //   value := "b01010".U
  //   enabler := false.B
  //  }
  //   io.cnt_s := (value - 1.U) % 9.U

    when(value === "b111".U) {
      // enabler := 0.U
      value := "b000".U
    }
}

/** shift register class */
class ShiftRegister extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
     val rxd = Input(UInt(1.W))
     val data = Output(UInt(8.W))
    })

  // internal variables
  /* 
   * TODO: Define internal variables (registers and/or wires), if needed
   */
   val result = RegInit("b11111111".U(8.W))

  // functionality
  /* 
   * TODO: Describe functionality if the shift register
   */
  //  printf("rxd in ShiftRegister = %b \n", io.rxd)
   result := (result << 1) + io.rxd
   io.data := result
}

/** 
  * The last warm-up task deals with a more complex component. Your goal is to design a serial receiver.
  * It scans an input line (“serial bus”) named rxd for serial transmissions of data bytes. A transmission 
  * begins with a start bit ‘0’ followed by 8 data bits. The most significant bit (MSB) is transmitted first. 
  * There is no parity bit and no stop bit. After the last data bit has been transferred a new transmission 
  * (beginning with a start bit, ‘0’) may immediately follow. If there is no new transmission the bus line 
  * goes high (‘1’, this is considered the “idle” bus signal). In this case the receiver waits until the next 
  * transmission begins. The outputs of the design are an 8-bit parallel data signal and a valid signal. 
  * The valid signal goes high (‘1’) for one clock cycle after the last serial bit has been transmitted, 
  * indicating that a new data byte is ready.
  */
class ReadSerial extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a the component as stated in the documentation
     */
     val rxd = Input(UInt(1.W))
     val reset_n = Input(UInt(1.W))
     val valid = Output(UInt(1.W))
     val data = Output(UInt(8.W))
    })


  // instanciation of modules
  /* 
   * TODO: Instanciate the modules that you need
   */
   val rs_controller = Module(new Controller())
   val rs_counter = Module(new Counter())
   val rs_shiftreg = Module(new ShiftRegister())

  // connections between modules
  /* 
   * TODO: connect the signals between the modules
   */
   rs_controller.io.rxd := io.rxd
   rs_controller.io.reset_n := io.reset_n
   rs_controller.io.cnt_s := rs_counter.io.cnt_s

   rs_counter.io.cnt_en := rs_controller.io.cnt_en
   rs_counter.io.reset_n := io.reset_n

   rs_shiftreg.io.rxd := io.rxd

  // global I/O 
  /* 
   * TODO: Describe output behaviour based on the input values and the internal signals
   */

   io.valid := rs_controller.io.valid
   io.data := rs_shiftreg.io.data

}
