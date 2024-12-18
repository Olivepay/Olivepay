import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAtom } from 'jotai';
import { useQueries } from '@tanstack/react-query';
import { userAtom } from '../atoms';
import { useSnackbar } from 'notistack';
import { Swiper, SwiperSlide } from 'swiper/react';

import 'swiper/css';

import {
  ChatBubbleLeftIcon,
  Cog6ToothIcon,
  PencilSquareIcon,
  CreditCardIcon,
  BuildingStorefrontIcon,
  InboxArrowDownIcon,
} from '@heroicons/react/24/solid';

import {
  CreditCard,
  Layout,
  NavigateBox,
  Loader,
  Button,
  Modal,
} from '../component/common';

import { MyCoupon, NicknameChange, PasswordChange } from '../component/user';
import { UserInfo } from '../component/user';
import { creditCardAtom } from '../atoms/userAtom';
import { getUsersInfo } from '../api/userApi';
import { getCardsInfo, removeCard } from '../api/cardApi';
import { Helmet } from 'react-helmet';
import { logout } from '../api/loginApi';
import Cookies from 'js-cookie';

const MyPage = () => {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [user, setUser] = useAtom(userAtom);
  const [cards, setCards] = useAtom(creditCardAtom);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState<
    'nickname' | 'password' | 'coupon' | null
  >(null);

  const openModal = useCallback(
    (contentType: 'nickname' | 'password' | 'coupon') => {
      setModalContent(contentType);
      setIsModalOpen(true);
    },
    [],
  );

  const closeModal = useCallback(() => {
    setIsModalOpen(false);
    setModalContent(null);
  }, []);

  const queries = useQueries({
    queries: [
      {
        queryKey: ['user'],
        queryFn: getUsersInfo,
      },
      {
        queryKey: ['card'],
        queryFn: getCardsInfo,
      },
    ],
  });

  const [
    { data: userData, error: userError, isLoading: userLoading },
    { data: cardData, error: cardError, isLoading: cardLoading, refetch },
  ] = queries;

  useEffect(() => {
    if (userData) {
      setUser(userData);
    }
  }, [userData, setUser]);

  useEffect(() => {
    if (cardData) {
      setCards(cardData);
    }
  }, [cardData, setCards]);

  const handleAddCard = useCallback(() => {
    navigate('/card');
  }, [navigate]);

  const handleLogout = useCallback(async () => {
    try {
      await logout();
    } catch {
      enqueueSnackbar('로그아웃 요청이 실패했습니다. 토큰을 초기화합니다.', {
        variant: 'warning',
      });
    } finally {
      localStorage.clear();
      Cookies.remove('refreshToken');
      enqueueSnackbar('로그아웃 되었습니다', { variant: 'info' });
      navigate('/');
    }
  }, [enqueueSnackbar, navigate]);

  const deleteCard = useCallback(
    async (cardId: string) => {
      try {
        await removeCard(+cardId);
        await refetch();
        enqueueSnackbar('카드가 삭제되었습니다.', { variant: 'success' });
      } catch {
        enqueueSnackbar('카드 삭제에 실패했습니다. 나중에 다시 시도해주세요.', {
          variant: 'error',
        });
      }
    },
    [refetch, enqueueSnackbar],
  );

  const cardDisplayNames = useMemo(() => {
    return cards?.map((card) => ({
      cardId: card.cardId,
      displayName: `${card.cardCompany} ${card.realCardNumber.slice(-4)}`,
      realCardNumber: card.realCardNumber,
      isDefault: card.isDefault,
    }));
  }, [cards]);

  if (userLoading || cardLoading) return <Loader />;

  if (userError || cardError) return <div>에러</div>;

  return (
    <>
      <Helmet>
        <meta
          name="description"
          content="결식 아동의 메인페이지이며 유저 정보를 확인하고 이용 가능한 서비스로 이동이 가능합니다."
        />
      </Helmet>
      <Layout>
        <main>
          <section className="bg-LIGHTBASE bg-opacity-50 pb-8 pt-4 shadow-md">
            <div className="mb-2 mr-2 text-end">
              <Button variant="text" label="로그아웃" onClick={handleLogout} />
            </div>
            <div className="flex gap-2 px-4">
              <UserInfo user={user} className="h-42 flex-grow-[3] bg-white" />
              <div className="flex flex-grow flex-col gap-2">
                <NavigateBox
                  className="h-20 bg-white"
                  path="/mypage/nickname/edit"
                  icon={<PencilSquareIcon className="size-6 text-PRIMARY" />}
                  onClick={() => openModal('nickname')}
                  text="닉네임"
                />
                <NavigateBox
                  className="h-20 bg-white"
                  path="/mypage/password/edit"
                  icon={<Cog6ToothIcon className="size-6 text-PRIMARY" />}
                  onClick={() => openModal('password')}
                  text="비밀번호"
                />
              </div>
            </div>
          </section>
          <section className="my-8">
            <h2 className="my-4 text-center text-lg font-bold text-DARKBASE">
              보유 카드
            </h2>
            <div className="pl-2">
              <Swiper slidesPerView={1.3} centeredSlides={true}>
                {cardDisplayNames?.map((card) => (
                  <SwiperSlide
                    key={card.cardId}
                    style={{
                      display: 'flex',
                      justifyContent: 'center',
                      alignItems: 'center',
                    }}
                  >
                    <CreditCard
                      cardName={card.displayName}
                      cardNumber={card.realCardNumber}
                      cardOwner={user.name}
                      isDefault={card.isDefault}
                      onClick={() => deleteCard(card.cardId)}
                    />
                  </SwiperSlide>
                ))}
                <SwiperSlide
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                  }}
                >
                  <div
                    className="flex h-44 w-64 cursor-pointer items-center justify-center rounded-lg border-2 border-dashed border-BASE"
                    onClick={handleAddCard}
                  >
                    <div className="text-center text-DARKBASE">
                      <span className="text-4xl">+</span>
                      <p className="text-base">카드 추가</p>
                    </div>
                  </div>
                </SwiperSlide>
              </Swiper>
            </div>
          </section>
          <section
            className="mb-24 bg-LIGHTBASE bg-opacity-50 pb-6 pt-2"
            style={{
              boxShadow:
                '0 -4px 6px -1px rgba(0, 0, 0, 0.1), 0 -2px 4px -1px rgba(0, 0, 0, 0.06), 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
            }}
          >
            <NavigateBox
              className="m-4 h-20 bg-white"
              path="/mypage/coupon"
              onClick={() => openModal('coupon')}
              icon={<InboxArrowDownIcon className="size-6 text-PRIMARY" />}
              text="보유 쿠폰"
            />
            <div className="flex gap-2 px-4">
              <div className="flex flex-1 flex-col gap-2">
                <NavigateBox
                  className="h-20 bg-white"
                  path="/review"
                  icon={<ChatBubbleLeftIcon className="size-6 text-PRIMARY" />}
                  text="리뷰 관리"
                />
                <NavigateBox
                  className="h-20 bg-white"
                  path="/like"
                  icon={
                    <BuildingStorefrontIcon className="size-6 text-PRIMARY" />
                  }
                  text="찜한 식당"
                />
              </div>
              <NavigateBox
                className="h-42 flex-1 bg-white"
                path="/history"
                icon={<CreditCardIcon className="size-6 text-PRIMARY" />}
                text="결제 내역"
                bigger={true}
              />
            </div>
          </section>
        </main>
        <Modal isOpen={isModalOpen} onClose={closeModal}>
          {modalContent === 'nickname' && (
            <NicknameChange closeModal={closeModal} />
          )}
          {modalContent === 'password' && (
            <PasswordChange closeModal={closeModal} />
          )}
          {modalContent === 'coupon' && <MyCoupon />}
        </Modal>
      </Layout>
    </>
  );
};

export default MyPage;
