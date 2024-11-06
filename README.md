# All data were collected in accordance with the recommendations outlined in the ITU-T P.809 document. Our focus is exclusively on the performance of cloud gaming and user Quality of Experience (QoE); aspects such as immersion and game content were not evaluated.
# First and second datasets were collected in a wired testbed and third dataset in a wireless testbed with a smartphone as the client device. All details of both testbeds are in the articles below.

>This repository contains data collected from the experiments of Daniel Henriques C. M. Soares.
>These data have been used in the following studies to date(06/11/24):

>Enhancing Cloud Gaming QoE Estimation by Stacking Learning - Journal of Network and Systems Management - 10.1007/s10922-024-09836-6

>A Stacking Learning-Based QoE Model for Cloud Gaming - NOMS 2023-2023 IEEE/IFIP Network Operations and Management Symposium - 10.1109/NOMS56928.2023.10154380

>Transfer Learning-Based QoE Estimation For Different Cloud Gaming Contexts - 2023 IEEE 9th International Conference on Network Softwarization (NetSoft) - 10.1109/NetSoft57336.2023.10175441

>A Human-in-the-Loop Based ML Framework to Estimate User’s QoE on Cloud Gaming Using Active Learning - 2024 Joint European Conference on Networks and Communications & 6G Summit (EuCNC/6G Summit) - 10.1109/EuCNC/6GSummit60053.2024.10597003

>QoE Estimation Across Different Cloud Gaming Services Using Transfer Learning - IEEE Transactions on Network and Service Management - 10.1109/TNSM.2024.3451300


#Dataset Description:

>QoE - Label of QoE from 0 to 6, these being: extremely bad, very bad, bad, fair, good, excellent, and ideal.

>UsoCpu, UsoRam,UsoGpu and UsoVram were used to garantee that the hardware was not limiting the experiment.

>DelayComandos, JitterComandos and PerdaComandos are Input Delay, Jitter and Loss respectively.

>DelayVideo, JitterVideo and PerdaVideo are Video Delay, Jitter and Loss respectively.

>Jogo - Game played

>TerminoPartida - Time(HH.MM) of end of playtime

>JogariaNovamente - Would you play again like that? YES (SIM) or NO(NAO)

>EstadoEspirito - Are you happy? Happy (Feliz), Indiferent(Indiferente) and Sad (Triste)

>IDADE - Age

>GENERO - Gender

>CURSO - level of educational attainment

>HORASJOGASEMANA - Number of hours of videogames played at home in a weeek

>MOUSEMANETEVR - What hardware is used to play at home. "Nenhum" means no one.

>RESOLUCAO - Resolution of the monitor/tv used at home (When they dont know, we used -1080p, or less than 1080p)

>FREQUENCIA - Refresh rate of the monitor used to play at home (When they dont know, we used 60)

>QUALJAJOGOU	- if yhey played any game of the experiment before.

>TEMPOQUALJAJOGOU - if the last question was anwsered, for how long in hours?
