
# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/P_1.c 

LD_SRCS += \
../src/lscript.ld 

OBJS += \
./src/P_1.o 

C_DEPS += \
./src/P_1.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: ARM gcc compiler'
	arm-xilinx-eabi-gcc -Wall -O0 -g3 -c -fmessage-length=0 -I../../##PROCESSOR_NAME##_bsp/##PROCESS_NAME##/include -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


