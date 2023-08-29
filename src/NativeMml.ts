import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import { type Mml } from 'react-native-mml';

export interface Spec extends TurboModule {
  getAll(options: Mml): Promise<Mml>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Mml');
