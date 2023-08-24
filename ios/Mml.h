
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNMmlSpec.h"

@interface Mml : NSObject <NativeMmlSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Mml : NSObject <RCTBridgeModule>
#endif

@end
