.section .data
menu_msg:    .asciz "Select operation: + - * /\n"
input1_msg:  .asciz "Enter first 64-bit hex number: "
input2_msg:  .asciz "Enter second 64-bit hex number: "
result_msg:  .asciz "Result: 0x%lx\n"
input_fmt:   .asciz "%lx"

.section .bss
input1:    .space 8
input2:    .space 8
operation: .space 1

.section .text
.global _start
.extern printf
.extern scanf

# Macro to print a string
.macro print_str msg
    movq $1, %rax
    movq $1, %rdi
    leaq \msg(%rip), %rsi
    movq $29, %rdx
    syscall
.endm

# Macro to read user input
.macro read_char dest
    movq $0, %rax
    movq $0, %rdi
    leaq \dest(%rip), %rsi
    movq $1, %rdx
    syscall
.endm

# Entry Point
_start:
    # Display menu
    print_str menu_msg
    read_char operation

    # Ask for first number
    leaq input1_msg(%rip), %rdi
    call printf
    leaq input1(%rip), %rsi
    leaq input_fmt(%rip), %rdi
    call scanf

    # Ask for second number
    leaq input2_msg(%rip), %rdi
    call printf
    leaq input2(%rip), %rsi
    leaq input_fmt(%rip), %rdi
    call scanf

    # Load inputs into registers
    movq input1(%rip), %r8    # first number in r8
    movq input2(%rip), %r9    # second number in r9

    # Perform operation - Switch Case Simulation
    movb operation(%rip), %al  # get operation character

    cmpb $'+', %al
    je perform_addition

    cmpb $'-', %al
    je perform_subtraction

    cmpb $'*', %al
    je perform_multiplication

    cmpb $'/', %al
    je perform_division

    jmp end_program  # if invalid input

# Addition Procedure
perform_addition:
    addq %r9, %r8
    jmp print_result

# Subtraction Procedure
perform_subtraction:
    subq %r9, %r8
    jmp print_result

# Multiplication Procedure
perform_multiplication:
    movq %r8, %rax
    mulq %r9            # result in rdx:rax
    movq %rax, %r8      # use lower 64-bits as result
    jmp print_result

# Division Procedure
perform_division:
    xorq %rdx, %rdx     # clear remainder
    movq %r8, %rax
    divq %r9            # rax = quotient
    movq %rax, %r8      # result into r8
    jmp print_result

# Print Result Procedure
print_result:
    leaq result_msg(%rip), %rdi
    movq %r8, %rsi
    call printf
    jmp end_program

end_program:
    movq $60, %rax  # syscall: exit
    xorq %rdi, %rdi
    syscall
