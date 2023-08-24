import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getAll(options: any): Promise<any>;
  getCheck(): Promise<any>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Mml');
