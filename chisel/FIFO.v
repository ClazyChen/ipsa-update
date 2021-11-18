module SRAM(
  input         clock,
  input         io_w_en,
  input  [7:0]  io_w_addr,
  input  [63:0] io_w_data,
  input         io_r_en,
  input  [7:0]  io_r_addr,
  output [63:0] io_r_data
);
`ifdef RANDOMIZE_MEM_INIT
  reg [63:0] _RAND_0;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_REG_INIT
  reg [63:0] mem [0:255]; // @[sram.scala 31:26]
  wire [63:0] mem_io_r_data_MPORT_data; // @[sram.scala 31:26]
  wire [7:0] mem_io_r_data_MPORT_addr; // @[sram.scala 31:26]
  wire [63:0] mem_MPORT_data; // @[sram.scala 31:26]
  wire [7:0] mem_MPORT_addr; // @[sram.scala 31:26]
  wire  mem_MPORT_mask; // @[sram.scala 31:26]
  wire  mem_MPORT_en; // @[sram.scala 31:26]
  reg  mem_io_r_data_MPORT_en_pipe_0;
  reg [7:0] mem_io_r_data_MPORT_addr_pipe_0;
  assign mem_io_r_data_MPORT_addr = mem_io_r_data_MPORT_addr_pipe_0;
  assign mem_io_r_data_MPORT_data = mem[mem_io_r_data_MPORT_addr]; // @[sram.scala 31:26]
  assign mem_MPORT_data = io_w_data;
  assign mem_MPORT_addr = io_w_addr;
  assign mem_MPORT_mask = 1'h1;
  assign mem_MPORT_en = io_w_en;
  assign io_r_data = mem_io_r_data_MPORT_data; // @[sram.scala 36:24 sram.scala 37:23]
  always @(posedge clock) begin
    if(mem_MPORT_en & mem_MPORT_mask) begin
      mem[mem_MPORT_addr] <= mem_MPORT_data; // @[sram.scala 31:26]
    end
    if (io_w_en) begin
      mem_io_r_data_MPORT_en_pipe_0 <= 1'h0;
    end else begin
      mem_io_r_data_MPORT_en_pipe_0 <= io_r_en;
    end
    if (io_w_en ? 1'h0 : io_r_en) begin
      mem_io_r_data_MPORT_addr_pipe_0 <= io_r_addr;
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {2{`RANDOM}};
  for (initvar = 0; initvar < 256; initvar = initvar+1)
    mem[initvar] = _RAND_0[63:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_1 = {1{`RANDOM}};
  mem_io_r_data_MPORT_en_pipe_0 = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  mem_io_r_data_MPORT_addr_pipe_0 = _RAND_2[7:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module FIFO(
  input         clock,
  input         reset,
  input         io_enq_en,
  input  [63:0] io_enq_data,
  output        io_enq_valid,
  input         io_deq_en,
  output [63:0] io_deq_data,
  output        io_deq_valid
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  wire  sram_clock; // @[fifo.scala 24:22]
  wire  sram_io_w_en; // @[fifo.scala 24:22]
  wire [7:0] sram_io_w_addr; // @[fifo.scala 24:22]
  wire [63:0] sram_io_w_data; // @[fifo.scala 24:22]
  wire  sram_io_r_en; // @[fifo.scala 24:22]
  wire [7:0] sram_io_r_addr; // @[fifo.scala 24:22]
  wire [63:0] sram_io_r_data; // @[fifo.scala 24:22]
  reg [7:0] front; // @[fifo.scala 26:24]
  reg [7:0] back; // @[fifo.scala 27:24]
  wire [7:0] next_front = front + 8'h1; // @[fifo.scala 29:28]
  wire [7:0] next_back = back + 8'h1; // @[fifo.scala 30:26]
  wire  nfull = front != next_back; // @[fifo.scala 31:24]
  wire  nempty = front != back; // @[fifo.scala 32:24]
  wire  wen = nfull & io_enq_en; // @[fifo.scala 38:21]
  wire  ren = nempty & io_deq_en; // @[fifo.scala 47:22]
  SRAM sram ( // @[fifo.scala 24:22]
    .clock(sram_clock),
    .io_w_en(sram_io_w_en),
    .io_w_addr(sram_io_w_addr),
    .io_w_data(sram_io_w_data),
    .io_r_en(sram_io_r_en),
    .io_r_addr(sram_io_r_addr),
    .io_r_data(sram_io_r_data)
  );
  assign io_enq_valid = front != next_back; // @[fifo.scala 31:24]
  assign io_deq_data = sram_io_r_data; // @[fifo.scala 50:20]
  assign io_deq_valid = front != back; // @[fifo.scala 32:24]
  assign sram_clock = clock;
  assign sram_io_w_en = nfull & io_enq_en; // @[fifo.scala 38:21]
  assign sram_io_w_addr = back; // @[fifo.scala 40:20]
  assign sram_io_w_data = io_enq_data; // @[fifo.scala 41:20]
  assign sram_io_r_en = nempty & io_deq_en; // @[fifo.scala 47:22]
  assign sram_io_r_addr = front; // @[fifo.scala 49:20]
  always @(posedge clock) begin
    if (reset) begin // @[fifo.scala 26:24]
      front <= 8'h0; // @[fifo.scala 26:24]
    end else if (ren) begin // @[fifo.scala 51:16]
      front <= next_front; // @[fifo.scala 52:15]
    end
    if (reset) begin // @[fifo.scala 27:24]
      back <= 8'h0; // @[fifo.scala 27:24]
    end else if (wen) begin // @[fifo.scala 42:16]
      back <= next_back; // @[fifo.scala 43:14]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  front = _RAND_0[7:0];
  _RAND_1 = {1{`RANDOM}};
  back = _RAND_1[7:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
