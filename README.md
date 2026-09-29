# Experience Ore 1.0.0

Fabric • Minecraft 1.21–1.21.11 e 26.1–26.3 • ID do mod: `xpore`

Minérios comuns em veios, com base de pedra/ardósia visível, cristais verdes 3D,
três partículas orbitais com rastro e fade suave após a quebra. Não há crescimento,
geodos, BlockEntity ou comentários nas classes Java.

## Estado desta revisão

Esta revisão retoma o projeto 1.3.0, com shards integrados, pedra/deepslate e
três órbitas. A numeração passa a 1.0.0. Não inclui o bloco independente
experience_cluster nem a textura de base inspirada em budding amethyst.

A textura dos shards foi redesenhada com base na foto enviada: hastes finas
verde-escuras, cristal central mais alto e pontas verde-claro quase brancas.
A emissão dos shards foi preservada conforme o ZIP enviado, com intensidade
maior nas pontas claras. A pedra e suas incrustações verdes não emitem luz.

## Compilar e instalar

Este projeto inclui 17 alvos Fabric: 1.21 a 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 e 26.3.
Extraia em uma pasta nova. Selecione **JDK 25 para iniciar o Gradle**; os alvos 1.21.x são compilados para Java 21 e 26.x para Java 25.

```powershell
.\gradlew.bat chiseledBuild
```

Os JARs ficam em `build/libs/1.0.0/`. Instale somente o arquivo da versão exata do seu Minecraft, sem `-sources`, junto da Fabric API correspondente. Cliente e servidor precisam do mod.
Veja [MULTIVERSION.md](MULTIVERSION.md) para selecionar o Java, compilar um alvo e usar GitHub Actions.

## IDs e inventário criativo

| ID novo | Inglês | Português | Dureza | Geração |
| --- | --- | --- | --- | --- |
| `xpore:experience_ore` | Experience Ore | Minério de Experiência | 3.0 | Y 0–40 |
| `xpore:deepslate_experience_ore` | Deepslate Experience Ore | Minério de Experiência Profundo | 4.5 | Y -20–0 |

Ambos aparecem em **Natural Blocks**, depois do minério de esmeralda de ardósia.
O evento usa a chave `minecraft:natural_blocks`, equivalente a `ItemGroups.NATURAL`.
Uma versão inicial usava `minecraft:natural`, que não correspondia à aba real.

```mcfunction
/give @s xpore:experience_ore 64
/give @s xpore:deepslate_experience_ore 64
```

**Para quem vem de versões anteriores à 1.2.0:** a mudança de IDs feita na 1.2.0 não migra mundos antigos automaticamente. Os identificadores
`xpore:xp_ore` e `xpore:deepslate_xp_ore` não são mais registrados. Blocos/itens
salvos com esses nomes podem desaparecer ao carregar com a nova versão. Use uma
cópia do mundo para testar e migre esses IDs em blocos, inventários e comandos
antes de usar esta versão no mundo principal. Os IDs desta revisão são os mesmos da base 1.3.0. A configuração continua sendo
`config/xpore.json` e mantém suas opções.

## Cristais e emissão

Os cristais seguem a nova referência: um shard central alto e dois
laterais finos, com hastes verde-escuras e pontas verde-claro quase brancas. A silhueta pixelada
é aplicada a dois planos cruzados por face, no estilo dos modelos de ametista.
Há um conjunto no topo, embaixo e em cada lateral, sem crescimento ou estados direcionais.

A pedra e a ardósia têm pequenas incrustações verdes visíveis. Essa textura usa
uma camada recortada sobre a base, preservando a pedra do Minecraft ou resource pack.
Os cristais e as incrustações são materiais distintos; somente os cristais têm emissão, com intensidade maior nas partes claras. As incrustações verdes da pedra recebem iluminação normal.

O material dos cristais é separado da pedra:

- `experience_crystal.png`: textura verde visível, presente mesmo sem shaders.
- `experience_crystal_e.png`: overlay emissivo das pontas dos cristais, padrão OptiFine.
- `experience_crystal_s.png`: mapa LabPBR; emissividade no canal alpha.
- `experience_crystal_n.png`: normal neutra LabPBR.
- `experience_inclusions.png`: incrustações verdes recortadas sobre a pedra; `_e` transparente e emissão zero no `_s`.

O bloco inteiro tem luminância **zero**. Não há mapa emissivo aplicado à pedra,
nem classificação do bloco inteiro como fonte de luz. A aura mantém brilho próprio.

Para shaders com **emissão LabPBR**, habilite o suporte a materiais/PBR e emissão
nas opções do shader. Em um ambiente que leia emissivas OptiFine, `_e` é ativado
por `assets/minecraft/optifine/emissive.properties`. Iris sozinho não garante a
leitura de overlays `_e`; em Fabric, um leitor compatível como Continuity pode
fornecer esse caminho. OptiFine não é uma dependência necessária deste mod Fabric.

Não existe garantia universal entre shader packs: alguns não leem emissão
LabPBR, outros impõem iluminação por ID de bloco, e bloom depende do shader.
Os mapas fornecidos isolam os cristais; o shader precisa respeitar
esses materiais. Sem um leitor emissivo ou shader compatível, a geometria verde
continua visível, mas recebe iluminação normal. Veja `MODEL.md` para os canais.

## Órbita e rastro

`randomDisplayTick` solicita **três partículas principais** por bloco exposto.
Cada uma mantém sua própria órbita, e a fábrica evita criar cópias da mesma partícula.
Todas giram ao redor do centro do bloco, a uma distância fixa de **1.04 bloco**:

- Plano XZ: horizontal, girando em torno do eixo Y.
- Plano XY: vertical, girando em torno do eixo Z.
- Plano YZ: vertical, girando em torno do eixo X.

As órbitas são círculos fechados em planos fixos e perpendiculares, com fases
diferentes. Nas verticais, a altura oscila ao completar a volta; não há subida
contínua nem espiral. O período é aproximadamente 4.8 segundos a 20 TPS.

Cada cabeça cria dois segmentos de rastro por tick. Os segmentos ficam onde
nasceram e perdem opacidade/tamanho ao longo de 12 ticks. Ao quebrar o minério,
as cabeças continuam orbitando durante 20 ticks (~1 segundo), com fade suave,
e os últimos rastros terminam de desaparecer em até mais 12 ticks.

Pontos dentro de blocos opacos ficam ocultos. Em uma parede ou no chão, parte
de uma órbita pode ficar escondida; coloque o bloco isolado para ver os três
planos completos. Opções gráficas de partículas podem reduzir a densidade.
Ao sair da área de atualizações visuais, a aura expira após alguns segundos.

## Configuração e XP

O arquivo `config/xpore.json` é criado na primeira inicialização:

```json
{
  "minXpDrop": 3,
  "maxXpDrop": 8,
  "fortuneMultiplier": 1.5,
  "oreGenerationRarity": 2,
  "enableDeepslateVariant": true
}
```

Reinicie o jogo inteiro/servidor após alterar. No multiplayer, o servidor
controla XP e geração. `/reload` não relê a config.

Sem Toque Suave, sorteia-se XP entre o mínimo e o máximo, inclusivos:

`XP = floor(base × fortuneMultiplier ^ nível_de_Fortuna)`

Com os defaults: sem Fortuna 3–8; Fortuna I 4–12; Fortuna II 6–18;
Fortuna III 10–27. Nem todos os valores intermediários são possíveis.
Toque Suave entrega um bloco da variante minerada e zero XP. Requer picareta
de ferro ou superior; madeira, pedra e ouro não dão recompensa. As loot tables
são vazias porque os drops são criados pelo código. `doTileDrops false` bloqueia
item e XP. Explosões, comandos e criativo não geram recompensa de mineração.

Limites: XP base 0–10000; máximo nunca abaixo do mínimo; multiplicador 1–10;
raridade 1–10000. Valores fora da faixa são ajustados e persistidos. Arquivos
inválidos são preservados em `.bak` antes de restaurar defaults. Há um teto
numérico defensivo de 1.000.000 de XP para encantamentos extremos por comandos.

`oreGenerationRarity` maior significa geração mais rara: cada uma das quatro
candidaturas por chunk/variante passa com chance `1 / raridade`. O padrão 2 dá,
em média, duas candidaturas por chunk por variante, ainda sujeitas à rocha e
às condições de geração. Não é uma cópia estatística exata da esmeralda vanilla.

As features usam OreFeature com tamanho 3 e rochas correspondentes. As âncoras
3..40 e -17..0 reservam margem inferior para a geometria dos veios. A geração só
altera chunks novos. `enableDeepslateVariant=false` desativa a geração natural
profunda; o ID novo permanece registrado para preservar blocos/itens dessa
versão já existentes.

## Código e validação

Os arquivos Java estão completos e sem comentários em `src/main/java`, `src/test/java` e `versions/<versao>/src`. `AbstractExperienceOreBlock` compartilha
mineração e criação da aura; as duas classes concretas representam as variantes.
`XpAuraParticle` e `XpTrailParticle` implementam órbitas e rastros no cliente.
`OrbitMath` contém os três planos, raio, ângulo e curva de fade. Os demais registros e a config
mantêm a organização das versões anteriores.

`tools/make_assets.py` regenera modelos, texturas e dados (Python + Pillow),
sobrescrevendo esses recursos. É opcional; o build não usa Python.
`docs/model-preview.png` é uma renderização técnica dos JSONs/texturas, sem shaders,
e não uma captura do Minecraft. A validação efetiva está em `VALIDATION.md`.

O comando `gradlew.bat :1.21.1:configCheck` executa verificações de leitura/gravação da
config. O build também executa essa tarefa. O log inclui um arquivo inválido
proposital usado para verificar o backup.

Código/texturas próprias: MIT. Texturas do jogo são referenciadas, não incluídas
como assets independentes. Gradle Wrapper: Apache 2.0.
