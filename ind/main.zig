const std = @import("std");

const ALIGN = 1 << 0;
const MEMINFO = 1 << 1;
const FLAGS = ALIGN | MEMINFO;
const MAGIC = 0x1BADB002;
const CHECKSUM = -%(MAGIC + FLAGS);

export var multiboot_header: [3]i32 align(4) linksection(".multiboot") = .{ MAGIC, FLAGS, CHECKSUM };

const VGA_BUFFER_ADDR = 0xB8000;

const COLOR = 0x0F;

export fn _start() callconv(.c) noreturn {
    print("Hello, world!");

    while (true) {}
}

fn print(message: []const u8) void {
    const vga_buffer = @as([*]volatile u16, @ptrFromInt(VGA_BUFFER_ADDR));

    for (message, 0..) |char, i| {
        const item: u16 = (@as(u16, COLOR) << 8) | @as(u16, char);
        vga_buffer[i] = item;
    }
}
